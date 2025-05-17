package app.web;

import app.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.TestBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerRestApiTest {

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestNotificationPreference_happyPath() throws Exception {

        // build notificationPreferences here or in TestBuilder class
        /* NotificationPreference aRandomNotificationPreference = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .type(NotificationType.EMAIL)
                .contactInfo("text")
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

         */
        // build request
        when(notificationService.getPreferenceByUserId(any())).thenReturn(aRandomNotificationPreference());

        MockHttpServletRequestBuilder request = get("/api/v1/notifications/preferences")
                .param("userId", UUID.randomUUID().toString());

        // send request
        mockMvc.perform(request)
                .andExpect(status().isOk())
                // assert that the contract <-> vacation_planner api is correct
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("type").isNotEmpty())
                .andExpect(jsonPath("enabled").isNotEmpty())
                .andExpect(jsonPath("contactInfo").isNotEmpty());
    }

    @Test
    void postWithBodyToCreatePreference_returns201AndCorrectDtoStructure() throws Exception {

        // build UpsertNotificationPreference here or in TestBuilder class
        /* UpsertNotificationPreference aRandomUpsertNotificationPreference = UpsertNotificationPreference.builder()
                .userId(UUID.randomUUID())
                .type(NotificationTypeRequest.EMAIL)
                .contactInfo("text")
                .notificationEnabled(true)
                .build();
         */

        // build request
        when(notificationService.upsertPreference(any())).thenReturn(aRandomNotificationPreference());

        MockHttpServletRequestBuilder request = post("/api/v1/notifications/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsBytes(aRandomUpsertNotificationPreference()));

        // send request
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("userId").isNotEmpty())
                .andExpect(jsonPath("type").isNotEmpty())
                .andExpect(jsonPath("enabled").isNotEmpty())
                .andExpect(jsonPath("contactInfo").isNotEmpty());
    }
}
