package app.web.mapper;

import app.event.payload.NotificationResponseKafka;
import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationPreferenceResponse;
import app.event.payload.NotificationPreferenceResponseKafka;
import app.web.dto.NotificationResponse;
import app.web.dto.NotificationTypeRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static NotificationType fromNotificationTypeRequest(NotificationTypeRequest dto) {

        return switch (dto) {
            case EMAIL -> NotificationType.EMAIL;
        };
    }

    public static NotificationPreferenceResponse fromNotificationPreference(NotificationPreference dto) {

                return NotificationPreferenceResponse.builder()
                        .id(dto.getUserId())
                        .type(dto.getType())
                        .contactInfo(dto.getContactInfo())
                        .enabled(dto.isEnable())
                        .userId(dto.getUserId())
                        .build();
    }

    public static NotificationPreferenceResponseKafka fromNotificationPreferenceKafka(NotificationPreference dto) {

        return NotificationPreferenceResponseKafka.builder()
                .id(dto.getUserId())
                .type(dto.getType())
                .contactInfo(dto.getContactInfo())
                .enabled(dto.isEnable())
                .userId(dto.getUserId())
                .build();
    }

    public static NotificationResponse fromNotification(Notification entity) {

        return NotificationResponse.builder()
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdOn(entity.getCreatedOn())
                .type(entity.getType())
                .build();
    }

    public static NotificationResponseKafka fromNotificationKafka(Notification entity) {

        return NotificationResponseKafka.builder()
                .userId(entity.getUserId())
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdOn(entity.getCreatedOn())
                .type(entity.getType())
                .build();
    }
}


