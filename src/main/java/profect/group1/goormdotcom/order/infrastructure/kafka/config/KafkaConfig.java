package profect.group1.goormdotcom.order.infrastructure.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import profect.group1.goormdotcom.order.infrastructure.kafka.dto.DeliveryRequestMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.DeliveryStartMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.OrderCompletedMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.PaymentSuccessMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.StockDecreaseRequestMessage;
import profect.group1.goormdotcom.order.infrastructure.kafka.dto.StockResponseMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer 및 Consumer 설정
 */
@Configuration
public class KafkaConfig {

    // ========== Producer 설정 ==========
    
    private Map<String, Object> getProducerConfig(org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            kafkaProperties.getProducer().getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return configProps;
    }

    @Bean
    public ProducerFactory<String, StockDecreaseRequestMessage> stockProducerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(getProducerConfig(kafkaProperties));
    }

    @Bean
    public KafkaTemplate<String, StockDecreaseRequestMessage> stockKafkaTemplate(
            ProducerFactory<String, StockDecreaseRequestMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, DeliveryRequestMessage> deliveryProducerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(getProducerConfig(kafkaProperties));
    }

    @Bean
    public KafkaTemplate<String, DeliveryRequestMessage> deliveryKafkaTemplate(
            ProducerFactory<String, DeliveryRequestMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, OrderCompletedMessage> orderCompletedProducerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(getProducerConfig(kafkaProperties));
    }

    @Bean
    public KafkaTemplate<String, OrderCompletedMessage> orderCompletedKafkaTemplate(
            ProducerFactory<String, OrderCompletedMessage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ========== Consumer 설정 ==========
    
    private Map<String, Object> getConsumerConfig(org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            kafkaProperties.getConsumer().getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, 
            kafkaProperties.getConsumer().getGroupId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.lang.String");
        return configProps;
    }

    @Bean
    public ConsumerFactory<String, PaymentSuccessMessage> paymentConsumerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        Map<String, Object> props = getConsumerConfig(kafkaProperties);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentSuccessMessage.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessMessage> paymentKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentSuccessMessage> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessMessage> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DeliveryStartMessage> deliveryConsumerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        Map<String, Object> props = getConsumerConfig(kafkaProperties);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DeliveryStartMessage.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryStartMessage> deliveryKafkaListenerContainerFactory(
            ConsumerFactory<String, DeliveryStartMessage> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, DeliveryStartMessage> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, StockResponseMessage> stockResponseConsumerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        Map<String, Object> props = getConsumerConfig(kafkaProperties);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, StockResponseMessage.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StockResponseMessage> stockResponseKafkaListenerContainerFactory(
            ConsumerFactory<String, StockResponseMessage> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, StockResponseMessage> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}

