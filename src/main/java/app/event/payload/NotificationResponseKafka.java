package app.event.payload;

import app.model.NotificationStatus;
import app.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponseKafka {

    private UUID userId;

    private String subject;

    private LocalDateTime createdOn;

    private NotificationStatus status;

    private NotificationType type;

    @Builder.Default
    private boolean success = true;

    private String error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
