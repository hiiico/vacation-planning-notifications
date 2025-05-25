package app.service;

import app.model.NotificationPreference;
import app.repository.NotificationRepository;
import app.repository.PreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationPreferencesUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void givenNotExistingNotificationPreference_whenChangeNotificationPreference_thenThrowException() {

        UUID userid = UUID.randomUUID();
        boolean isNotificationEnable =true;
        when(preferenceRepository.findByUserId(userid)).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> notificationService.changeNotificationPreference(userid, isNotificationEnable));
    }

    @Test
    void givenExistingNotificationPreference_whenChangeNotificationPreference_thenExpectingEnableToChange() {

        UUID userid = UUID.randomUUID();
        NotificationPreference preference = NotificationPreference.builder()
                .isEnable(false)
                .build();
        when(preferenceRepository.findByUserId(userid)).thenReturn(Optional.of(preference));

        notificationService.changeNotificationPreference(userid, true);

        assertTrue(preference.isEnable());

        verify(preferenceRepository, times(1)).save(preference);
    }
}
