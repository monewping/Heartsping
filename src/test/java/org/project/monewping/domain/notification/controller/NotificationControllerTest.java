package org.project.monewping.domain.notification.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.notification.dto.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("Notification Controller 슬라이스 테스트")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private UUID userId;
    private UUID resourceId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /api/notifications – 알림 생성 성공")
    void testCreateNotification() throws Exception {
        NotificationDto dto = NotificationDto.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .resourceId(resourceId)
            .resourceType("Article")
            .content("영화와 관련된 기사가 2건 등록되었습니다.")
            .confirmed(false)
            .createdAt(Instant.now())
            .build();
        when(notificationService.create(userId, resourceId, "Article"))
            .thenReturn(List.of(dto));

        mockMvc.perform(post("/api/notifications")
                .param("userId", userId.toString())
                .param("resourceId", resourceId.toString())
                .param("resourceType", "Article")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].userId").value(userId.toString()))
            .andExpect(jsonPath("$[0].resourceId").value(resourceId.toString()))
            .andExpect(jsonPath("$[0].resourceType").value("Article"));
    }

    @Test
    @DisplayName("GET /api/notifications – 알림 조회 성공")
    void testGetNotifications() throws Exception {
        NotificationDto notificationDto = NotificationDto.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .resourceId(resourceId)
            .resourceType("Article")
            .content("영화와 관련된 기사가 3건 등록되었습니다.")
            .confirmed(false)
            .createdAt(Instant.now())
            .build();
        CursorPageResponseNotificationDto responseDto =
            new CursorPageResponseNotificationDto(
                List.of(notificationDto),null,null,1,1,false
            );
        when(notificationService.findNotifications(eq(userId), isNull(), isNull(), eq(10)))
            .thenReturn(responseDto);

        mockMvc.perform(get("/api/notifications")
                .param("limit", "10")
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
            .andExpect(jsonPath("$.content[0].resourceId").value(resourceId.toString()));
    }

    @Test
    @DisplayName("PATCH /api/notifications – 전체 알림 확인 처리 성공")
    void testConfirmAllNotifications() throws Exception {
        doNothing().when(notificationService).confirmAll(userId);

        mockMvc.perform(patch("/api/notifications")
                .header("Monew-Request-User-ID", userId.toString())
            )
            .andExpect(status().isOk());

        verify(notificationService).confirmAll(userId);
    }

    @Test
    @DisplayName("PATCH /api/notifications/{notificationId} – 알림 확인 처리 성공")
    void testConfirmNotification() throws Exception {
        // given
        UUID notificationId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
                .header("Monew-Request-User-ID", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // verify
        verify(notificationService).confirmNotification(userId, notificationId);
    }
}