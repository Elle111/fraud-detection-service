package com.app.fraud.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.core.KafkaTemplate;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.dto.event.FraudDecisionEvent;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class KafkaTestConfig {
    
    @Bean
    @Primary
    public ConsumerFactory<String, TransactionCreatedEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.app.fraud.dto.event");
        props.put(JsonDeserializer.TYPE_MAPPINGS, "transactionCreated:com.app.fraud.dto.event.TransactionCreatedEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), 
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(TransactionCreatedEvent.class)));
    }
    
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, TransactionCreatedEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
    
    @Bean
    @Primary
    public ProducerFactory<String, FraudDecisionEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), 
                new JsonSerializer<>());
    }
    
    @Bean
    @Primary
    public ProducerFactory<String, TransactionCreatedEvent> transactionProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), 
                new JsonSerializer<>());
    }
    
    @Bean
    @Primary
    public KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate(
            ProducerFactory<String, TransactionCreatedEvent> transactionProducerFactory) {
        return new KafkaTemplate<>(transactionProducerFactory);
    }
}
