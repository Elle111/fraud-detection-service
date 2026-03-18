package com.app.fraud.config;

import com.app.fraud.dto.event.TransactionCreatedEvent;
import com.app.fraud.dto.event.FraudDecisionEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
@EnableKafka
@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${app.kafka.consumer-group}")
    private String consumerGroup;
    
    @Bean
    public ConsumerFactory<String, TransactionCreatedEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
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
    public ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, TransactionCreatedEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TransactionCreatedEvent> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler(
            new DeadLetterPublishingRecoverer(kafkaTemplate(producerFactory())),
            new FixedBackOff(1000L, 2)
        ));
        return factory;
    }
    
    @Bean
    public ProducerFactory<String, TransactionCreatedEvent> transactionProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), 
                new JsonSerializer<>());
    }
    
    @Bean
    public KafkaTemplate<String, TransactionCreatedEvent> transactionKafkaTemplate(
            ProducerFactory<String, TransactionCreatedEvent> transactionProducerFactory) {
        return new KafkaTemplate<>(transactionProducerFactory);
    }
    
    @Bean
    public ProducerFactory<String, FraudDecisionEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), 
                new JsonSerializer<>());
    }
    
    @Bean
    public KafkaTemplate<String, FraudDecisionEvent> kafkaTemplate(
            ProducerFactory<String, FraudDecisionEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
