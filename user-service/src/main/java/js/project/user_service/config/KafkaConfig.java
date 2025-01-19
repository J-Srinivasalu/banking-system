package js.project.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import js.project.model.UserCreatedEvent;
import js.project.user_service.model.ObjectTest;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.topic.user-created}")
    private String topicName;

    @Bean
    public NewTopic createTopic() {
        return new NewTopic(topicName, 3, (short) 1);
    }

    @Bean
    public NewTopic createStringTopic() {
        return new NewTopic("string-event", 1, (short) 1);
    }
    @Bean
    public NewTopic createObjectTopic() {
        return new NewTopic("object-event", 1, (short) 1);
    }

    // Consumer Factory for UserCreatedEvent
    @Bean
    public ConsumerFactory<String, UserCreatedEvent> userCreatedConsumerFactory() {
        return createConsumerFactory(UserCreatedEvent.class);
    }

    // Consumer Factory for AnotherEvent
    @Bean
    public ConsumerFactory<String, ObjectTest> objectEventConsumerFactory() {
        return createConsumerFactory(ObjectTest.class);
    }

    @Bean
    public JsonDeserializer<UserCreatedEvent> jsonUserCreatedDeserializer() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule()) // If you're using Java 8+ Date/Time types
                // Add other ObjectMapper configurations here if needed
                .build();
        JsonDeserializer<UserCreatedEvent> jsonDeserializer = new JsonDeserializer<>(UserCreatedEvent.class, objectMapper);
        jsonDeserializer.trustedPackages("js.project.model");
        return jsonDeserializer;
    }

    @Bean
    public JsonDeserializer<ObjectTest> jsonObjectDeserializer() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule()) // If you're using Java 8+ Date/Time types
                // Add other ObjectMapper configurations here if needed
                .build();
        JsonDeserializer<ObjectTest> jsonDeserializer = new JsonDeserializer<>(ObjectTest.class, objectMapper);
        jsonDeserializer.trustedPackages("js.project.auth_service.model");
        return jsonDeserializer;
    }

    @Bean
    public  <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> valueType) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "js.project.model");

        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(valueType);
        jsonDeserializer.trustedPackages("js.project.model"); // Important!

//        ErrorHandlingDeserializer<T> deserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class); // Set the delegate

        return new DefaultKafkaConsumerFactory<>(props);
    }

    // Kafka Listener Container Factory for UserCreatedEvent
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> userCreatedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userCreatedConsumerFactory());
        return factory;
    }

    // Kafka Listener Container Factory for AnotherEvent
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ObjectTest> anotherEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ObjectTest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(objectEventConsumerFactory());
        return factory;
    }
}