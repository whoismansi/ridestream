# Ridestream

An event-driven ride matching platform built to explore the failure modes that show up
when state is spread across a message broker, a cache, and a database.

Drivers stream location updates through Kafka into a Redis geospatial index. When a rider
requests a ride, the matching service queries that index and assigns a driver. The
interesting part is not the CRUD around it, it is what happens when messages arrive twice,
arrive out of order, or arrive while another consumer is assigning the same driver.

## Architecture

```
driver-service ──▶ driver-locations ──▶ matching-service ──▶ redis geo index
     :8081            (kafka)                :8082              (GEOADD/GEORADIUS)
                                                │
rider-service  ──▶ ride-requests   ─────────────┘
     :8083            (kafka)                   │
                                                ▼
                                          postgres (rides)
```

Three Spring Boot services over Kafka, Redis and Postgres. Event contracts shared through
a `shared-models` module so producer and consumer cannot drift apart independently.

| Module | Port | Responsibility |
|---|---|---|
| `driver-service` | 8081 | driver registration, location and status updates; produces `driver-locations` |
| `matching-service` | 8082 | consumes locations into a Redis geo index; nearby-driver queries |
| `rider-service` | 8083 | rider registration, ride requests; produces `ride-requests` |
| `shared-models` | n/a | Kafka event contracts (`LocationUpdate`, `RideRequestEvent`, `DriverStatus`) |

### Design notes

- **Location updates are keyed by `driverId`** so every update for a driver lands on the
  same partition, preserving per-driver ordering while still allowing the topic to scale
  horizontally.
- **Only `AVAILABLE` drivers are held in the geo index.** A status change to `BUSY` or
  `OFFLINE` removes the driver from the index rather than filtering at query time, which
  keeps `GEORADIUS` results small.
- **`requestId` is the idempotency key** on `ride-requests`. Kafka delivery is at-least-once,
  so a consumer that matches on every delivery would assign two drivers to one ride.

## Status

Working end to end: driver and rider registration, location ingestion into the geo index,
and nearby-driver queries.

In progress: the matching engine that consumes `ride-requests` and assigns a driver. Until
it lands, ride requests are published but not yet acted on.

Planned next: idempotent consumers with dead-letter handling, lease-based protection
against double-booking a driver, Testcontainers integration tests, and a load harness to
measure throughput and match latency under a simulated driver fleet.

## Running it

Requires JDK 21, Maven 3.9+, Docker.

```bash
# 1. credentials. copy the template and choose your own values
cp .env.example .env
$EDITOR .env
ln -sf ../.env infrastructure/.env   # so docker compose picks it up

# 2. infrastructure (kafka, zookeeper, postgres, redis, kafka-ui)
cd infrastructure && docker compose up -d && cd ..

# 3. build everything from the repo root
mvn clean install

# 4. export the credentials for the maven services, then run each in its own terminal
set -a && source .env && set +a
mvn -pl driver-service spring-boot:run
mvn -pl matching-service spring-boot:run
mvn -pl rider-service spring-boot:run
```

Kafka UI is at http://localhost:8080. Postgres listens on 5433, Redis on 6379.

No credentials are stored in the repository. `docker compose` and the services both read
them from `.env`, which is gitignored; `.env.example` lists the required variables with
placeholder values. Compose fails fast with a named variable if any are missing, and
`driver-service` and `rider-service` will not start without `POSTGRES_USER` and
`POSTGRES_PASSWORD`.

### Smoke test

```bash
# register a driver
curl -X POST http://localhost:8081/api/drivers \
  -H 'content-type: application/json' \
  -d '{"name":"john doe","email":"john@ridestream.com","phone":"+1-555-0101"}'

# publish a location update for that driver
curl -X PUT http://localhost:8081/api/drivers/<driver-id>/location \
  -H 'content-type: application/json' \
  -d '{"latitude":42.3601,"longitude":-71.0589}'

# query the geo index through the matching service
curl "http://localhost:8082/api/matching/nearby?latitude=42.3601&longitude=-71.0589&radius=5000"
```

A Postman collection for driver-service is in [`docs/postman/`](docs/postman/).

## API

**driver-service** `:8081`
```
POST   /api/drivers                 register a driver
GET    /api/drivers                 list all
GET    /api/drivers/{id}            fetch one
GET    /api/drivers/status/{status} filter by status
PUT    /api/drivers/{id}/location   update location, produces to driver-locations
PUT    /api/drivers/{id}/status     update availability
DELETE /api/drivers/{id}            remove
```

**matching-service** `:8082`
```
GET    /api/matching/nearby?latitude=&longitude=&radius=   nearby available drivers
GET    /api/matching/available-count                       size of the geo index
```

**rider-service** `:8083`
```
POST   /api/riders                      register a rider
GET    /api/riders                      list all
GET    /api/riders/{id}                 fetch one
PUT    /api/riders/{id}                 update
DELETE /api/riders/{id}                 remove
POST   /api/ride-requests               create a request, produces to ride-requests
GET    /api/ride-requests/{id}          fetch one
GET    /api/ride-requests/rider/{id}    requests for a rider
DELETE /api/ride-requests/{id}          cancel
```

Every service exposes `/actuator/health`.

## Data

**Postgres** (schema `ridestream`) holds `drivers`, `riders`, `ride_requests`, `rides` and
`driver_location_history`. Schema and indexes are in
[`infrastructure/init-scripts/`](infrastructure/init-scripts/).

**Redis** holds `drivers:locations`, the sorted set backing `GEOADD` / `GEORADIUS`, plus
per-driver status keys.

**Kafka topics**

| Topic | Producer | Consumer | Key |
|---|---|---|---|
| `driver-locations` | driver-service | matching-service | `driverId` |
| `ride-requests` | rider-service | matching-service | `requestId` |

## Build

A Maven reactor rooted at the top-level `pom.xml`. `mvn clean install` builds all four
modules; `mvn -pl <module>` targets one.

## License

MIT

## Author

Mansi Zope, [github.com/whoismansi](https://github.com/whoismansi)
