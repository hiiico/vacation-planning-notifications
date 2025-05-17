package app;

import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationTypeRequest;
import app.web.dto.UpsertNotificationPreference;
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

    public static UpsertNotificationPreference aRandomUpsertNotificationPreference() {
        return UpsertNotificationPreference.builder()
                .userId(UUID.randomUUID())
                .type(NotificationTypeRequest.EMAIL)
                .contactInfo("text")
                .notificationEnabled(true)
                .build();
    }
}
