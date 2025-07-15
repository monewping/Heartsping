package org.project.monewping.domain.notification.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Notification 통합 테스트")
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID userId;
    private UUID resourceId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
    }

    @Test
    void testCreateAndGetNotifications() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .param("userId",     userId.toString())
                .param("resourceId", resourceId.toString())
                .param("resourceType", "Comment")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].userId").value(userId.toString()))
            .andExpect(jsonPath("$[0].resourceId").value(resourceId.toString()))
            .andExpect(jsonPath("$[0].resourceType").value("Comment"));

        mockMvc.perform(get("/api/notifications")
                .param("limit", "10")
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
            .andExpect(jsonPath("$.content[0].resourceId").value(resourceId.toString()))
            .andExpect(jsonPath("$.content[0].resourceType").value("Comment"));
    }
}
