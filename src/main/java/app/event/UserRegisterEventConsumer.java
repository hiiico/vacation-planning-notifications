package app.event;

import app.model.NotificationPreference;
import app.service.NotificationService;
import app.web.dto.NotificationPreferenceResponse;
import app.event.payload.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class UserRegisterEventConsumer {

    public final NotificationService notificationService;

    public UserRegisterEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // groupId -> spring.kafka.consumer.group-id=vacation-planning-notifications
    @KafkaListener(topics = "user-register-event.v1", groupId = "vacation-planning-notifications")
//    public void consumeEvent(UserRegisterEvent event) {
//
//        log.info("Successfully consumed event for user %s".formatted(event.getUserId()));
//    }

//    public void consumeEvent(Object event) {
//
//        if (event instanceof UpsertNotificationPreference pref) {
//            NotificationPreference notificationPreference = notificationService
//                    .upsertPreference(pref);
//
//            // send response
////            NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);
//
//            log.info("Successfully consumed event for user %s".formatted(notificationPreference.getUserId()));
////        } else if (event instanceof AnotherEvent another) {
////            log.info("Another event: {}", another);
////        }
//
//        }
//
//    }

    public void consumeEvent(UpsertNotificationPreference event) {

        NotificationPreference notificationPreference= notificationService
                .upsertPreference(event);

        // NotificationPreferenceResponse responseDto = DtoMapper.fromNotificationPreference(notificationPreference);

        log.info("Successfully consumed event for user %s".formatted(notificationPreference.getUserId()));

    }

}
