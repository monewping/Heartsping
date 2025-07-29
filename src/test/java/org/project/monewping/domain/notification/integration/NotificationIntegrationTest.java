package org.project.monewping.domain.notification.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.MonewpingApplication;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = MonewpingApplication.class)
@EnableAutoConfiguration(
    exclude = {
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
    }
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Notification 통합 테스트")
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;
    private UUID resourceId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        User user = User.builder()
            .email("binu@google.com")
            .nickname("binu")
            .password("pw123456")
            .isDeleted(false)
            .build();

        userRepository.saveAndFlush(user);

        userId = user.getId();
        resourceId = UUID.randomUUID();
    }

    @Test
    @DisplayName("알림 생성 후 조회 시 등록한 알림이 반환된다")
    void testCreateAndGetNotifications() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .param("userId", userId.toString())
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

    @Test
    @DisplayName("모든 알림 확인 시 confirmed 필드가 true로 변경")
    void testConfirmAllNotifications() throws Exception {
        notificationRepository.save(new Notification(userId, "Binu님이 나의 댓글을 좋아합니다.", resourceId, "Comment"));
        notificationRepository.save(new Notification(userId, "골프와 관련된 기사가 2건 등록되었습니다.", resourceId, "Article"));

        mockMvc.perform(patch("/api/notifications")
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        long totalUnreadNotification = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        assertThat(totalUnreadNotification).isZero();

        List<Notification> all = notificationRepository.findAll();
        assertThat(all).isNotEmpty()
            .allMatch(Notification::getConfirmed);
    }

    @Test
    @DisplayName("알림 확인 시 confirmed 필드가 true로 변경")
    void testConfirmNotification() throws Exception {
        // given
        Notification notification = new Notification(
            userId,
            "루피님이 나의 댓글을 좋아합니다.",
            resourceId,
            "Comment"
        );
        notificationRepository.saveAndFlush(notification);

        // when
        mockMvc.perform(patch("/api/notifications/" + notification.getId())
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // then
        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getConfirmed())
            .as("알림 확인 요청 후 confirmed 값이 true로 변경되어야 한다")
            .isTrue();
    }
}
