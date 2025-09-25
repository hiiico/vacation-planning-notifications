package app;

import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static NotificationPreference aRandomNotificationPreference() {

        return NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(NotificationType.EMAIL)
                .contactInfo("text")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static NotificationRequest aRandomUpsertNotificationPreference() {
        return NotificationRequest.builder()
                .userId(UUID.randomUUID())
                .subject("subject")
                .body("body")
                .build();
    }
}
