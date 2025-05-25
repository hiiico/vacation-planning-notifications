package app.event;

import app.event.payload.UserRegisterEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRegisterEventConsumer {

    // groupId -> spring.kafka.consumer.group-id=vacation-planning-notifications
    @KafkaListener(topics = "user-register-event.v1", groupId = "vacation-planning-notifications")
    public void consumeEvent(UserRegisterEvent event) {

        log.info("Successfully consumed event for user %s".formatted(event.getUserId()));
    }
}
