package app.event;

import app.event.payload.UpsertNotificationPreference;
import app.event.payload.NotificationPreferenceResponseKafka;
import app.model.NotificationPreference;
import app.service.NotificationService;
import app.web.mapper.DtoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class EventConsumer {

    private final NotificationService notificationService;
    private final KafkaTemplate<String, EventMessage<?>> replyKafkaTemplate;
    private final Executor taskExecutor;
    private final ObjectMapper objectMapper;
    private final String replyTopic;

    public EventConsumer(NotificationService notificationService,
                         KafkaTemplate<String, EventMessage<?>> replyKafkaTemplate,
                         Executor taskExecutor,
                         ObjectMapper objectMapper,
                         @Value("${app.kafka.reply-topic}") String replyTopic) {
        this.notificationService = notificationService;
        this.replyKafkaTemplate = replyKafkaTemplate;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
        this.replyTopic = replyTopic;
    }

    @KafkaListener(topics = "${app.kafka.input-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public CompletableFuture<Void> consumeEvent(JsonNode eventMessage) {
        return CompletableFuture.runAsync(() -> {
            try {
                String eventType = eventMessage.path("eventType").asText(null);
                JsonNode payload = eventMessage.path("payload");

                if (eventType == null) {
                    log.error("Received event without eventType: {}", eventMessage);
                    return;
                }

                switch (eventType) {
                    case "UPSERT_NOTIFICATION_PREFERENCE" ->
                            handleUpsertNotificationPreference(payload);

                    // extend with other cases

                    default -> log.warn("Unknown eventType received: {}", eventType);
                }
            } catch (Exception e) {
                log.error("Error processing event: {}", eventMessage, e);
            }
        }, taskExecutor);
    }

    private void handleUpsertNotificationPreference(JsonNode payload) {
        UUID userId = null;
        try {
            if (payload.hasNonNull("userId")) {
                userId = UUID.fromString(payload.get("userId").asText());
            }

            UpsertNotificationPreference event =
                    objectMapper.convertValue(payload, UpsertNotificationPreference.class);

            NotificationPreference pref = notificationService.upsertPreference(event);
            NotificationPreferenceResponseKafka response =
                    DtoMapper.fromNotificationPreferenceKafka(pref);

            sendReply("NOTIFICATION_PREFERENCE_RESPONSE", response);

        } catch (Exception e) {
            log.error("Failed to handle UPSERT_NOTIFICATION_PREFERENCE", e);

            NotificationPreferenceResponseKafka errorResponse =
                    NotificationPreferenceResponseKafka.builder()
                            .userId(userId) // might be null if missing
                            .success(false)
                            .error("Processing failed: " + e.getMessage())
                            .build();

            sendReply("NOTIFICATION_PREFERENCE_RESPONSE", errorResponse);
        }
    }

    private <T> void sendReply(String eventType, T response) {
        EventMessage<T> wrapped = new EventMessage<>(eventType, response);

        replyKafkaTemplate.send(replyTopic, wrapped)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Reply sent [{}] to topic {}", eventType, replyTopic);
                    } else {
                        log.error("Failed to send reply [{}]: {}", eventType, ex.getMessage());
                    }
                });
    }
}
