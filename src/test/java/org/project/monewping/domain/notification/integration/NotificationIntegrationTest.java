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
            .build();

        userRepository.saveAndFlush(user);

        userId = user.getId();
        resourceId = UUID.randomUUID();
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
            .andExpect(status().isNoContent());

        long totalUnreadNotification = notificationRepository.countByUserIdAndConfirmedFalse(userId);
        assertThat(totalUnreadNotification).isZero();

        List<Notification> all = notificationRepository.findAll();
        assertThat(all).isNotEmpty()
            .allMatch(Notification::isConfirmed);
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
            .andExpect(status().isNoContent());

        // then
        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.isConfirmed())
            .as("알림 확인 요청 후 confirmed 값이 true로 변경되어야 한다")
            .isTrue();
    }
}
