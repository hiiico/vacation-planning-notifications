package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.model.NotificationType;
import app.repository.NotificationRepository;
import app.repository.PreferenceRepository;
import app.web.dto.NotificationRequest;
import app.event.payload.UpsertNotificationPreference;
import app.web.mapper.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final PreferenceRepository preferenceRepository;
    private final MailSender mailSender;
    private final NotificationRepository  notificationRepository;

    @Autowired
    public NotificationService(PreferenceRepository preferenceRepository, MailSender mailSender, NotificationRepository notificationRepository) {
        this.preferenceRepository = preferenceRepository;
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
    }

    public NotificationPreference upsertPreference(UpsertNotificationPreference dto) {

        Optional<NotificationPreference> userNotificationPreferenceOptional = preferenceRepository.findByUserId(dto.getUserId());

        if(userNotificationPreferenceOptional.isPresent()) {
            NotificationPreference preference = userNotificationPreferenceOptional.get();
                    preference.setType(DtoMapper.fromNotificationTypeRequest(dto.getType()));
                    preference.setContactInfo(dto.getContactInfo());
                    preference.setEnable(dto.isNotificationEnabled());
                    preference.setUpdatedOn(LocalDateTime.now());

                    return preferenceRepository.save(preference);
        }

         NotificationPreference notificationPreference = NotificationPreference.builder()
                .userId(dto.getUserId())
                .type(DtoMapper.fromNotificationTypeRequest(dto.getType()))
                .isEnable(dto.isNotificationEnabled())
                .contactInfo(dto.getContactInfo())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        return preferenceRepository.save(notificationPreference);
    }

    public NotificationPreference getPreferenceByUserId(UUID userId) {

        return preferenceRepository.findByUserId(userId)
                .orElseThrow(
                        () -> new NullPointerException(
                                "Notification preference for user with id %s was not found.".formatted( userId)));
    }

    public Notification sendNotification(NotificationRequest notificationRequest) {

        UUID userid = notificationRequest.getUserId();
        NotificationPreference userPreference = getPreferenceByUserId(userid);

        if(!userPreference.isEnable()) {
            throw new IllegalArgumentException(
                    "User with id %s is not allow to receive notifications.".formatted(userid));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject(notificationRequest.getSubject());
        message.setText(notificationRequest.getBody());

        Notification notification = Notification.builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .createdOn(LocalDateTime.now())
                .userId(userid)
                .deleted(false)
                .type(NotificationType.EMAIL)
                .build();

        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.SUCCEEDED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn(
                    "There was an issue sending an email to %s due to %s."
                            .formatted(userPreference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationHistory(UUID userId) {

       return notificationRepository.findAllByUserIdAndDeletedIsFalse(userId);
    }

    public NotificationPreference changeNotificationPreference(UUID userId, boolean enable) {

        NotificationPreference notificationPreference = getPreferenceByUserId(userId);
        notificationPreference.setEnable(enable);
        return preferenceRepository.save(notificationPreference);
    }
}
