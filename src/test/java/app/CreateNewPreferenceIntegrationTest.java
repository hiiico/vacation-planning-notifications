package app;

import app.model.NotificationPreference;
import app.repository.PreferenceRepository;
import app.service.NotificationService;
import app.web.dto.NotificationTypeRequest;
import app.web.dto.UpsertNotificationPreference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class CreateNewPreferenceIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PreferenceRepository preferenceRepository;

//    @Test
//    void createNotificationPreferences_happyPath() {
//
//        // Given
//        UUID userId = UUID.randomUUID();
//        UpsertNotificationPreference upsertNotificationPreference = UpsertNotificationPreference.builder()
//                .userId(userId)
//                .type(NotificationTypeRequest.EMAIL)
//                .notificationEnabled(true)
//                .contactInfo("test@mail.com")
//                .build();
//
//        // When
//        notificationService.upsertPreference(upsertNotificationPreference);
//
//        // Then
//        List<NotificationPreference> preferences = preferenceRepository.findAll();
//        assertEquals(1, preferences.size());
//        assertThat(preferences.get(0).getUserId()).isEqualTo(userId);
//
//        // NotificationPreference preference = preferences.get(0);
//        // assertEquals(userId, preference.getUserId());
//    }
}
