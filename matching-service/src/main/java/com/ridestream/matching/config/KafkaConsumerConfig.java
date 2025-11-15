package com.ridestream.matching.config;

import com.ridestream.matching.model.LocationUpdate;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration
 * Sets up Kafka consumer to listen to driver-locations topic
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Configure consumer properties
     */
    @Bean
    public ConsumerFactory<String, LocationUpdate> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Kafka broker address
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Consumer group ID
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Key deserializer
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // converts JSON to LocationUpdate
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Trust all packages for deserialization
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Start from earliest message if no offset is found
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
            configProps,
            new StringDeserializer(),
            new JsonDeserializer<>(LocationUpdate.class, false)
        );
    }

    /**
     * Create Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LocationUpdate> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, LocationUpdate> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
