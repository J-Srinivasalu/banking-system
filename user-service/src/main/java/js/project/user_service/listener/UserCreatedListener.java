package js.project.user_service.listener;

import js.project.user_service.model.dto.UserCreatedEvent;
import js.project.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedListener {

    private final UserService userService;

    @KafkaListener(topics = "${kafka.topic.user-created}", groupId = "${kafka.consumer.group-id}")
    public void listenUserCreated(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent: {}", event);
        try {
            userService.createUser(event);
        } catch (Exception e) {
            log.error("Error handling UserCreatedEvent: {}", event, e);
            // todo: retry mechanism or dead-letter queue handling
        }
    }
}