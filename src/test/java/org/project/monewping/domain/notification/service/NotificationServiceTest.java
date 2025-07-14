package org.project.monewping.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.notification.dto.NotificationDto;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.global.dto.CursorPageResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 서비스 단위 테스트")
public class NotificationServiceTest {

    private static final int DEFAULT_LIMIT = 10;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private BasicNotificationService notificationService;

    private UUID userId;
    private CursorPageResponse<NotificationDto> emptyResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        emptyResponse = new CursorPageResponse<>(
            List.of(), null, null, 0, 0, false);
    }

    @Test
    @DisplayName("알림이 없으면 빈 페이지 응답을 반환한다")
    void returnEmptyPageWhenNoNotifications() {
        // given
        when(notificationRepository.findByUserIdAndAfter(userId, null, DEFAULT_LIMIT))
            .thenReturn(emptyResponse);

        // when
        var page = notificationService.findNotifications(userId, null, DEFAULT_LIMIT);

        // then
        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
        assertThat(page.hasNext()).isFalse();
    }
}