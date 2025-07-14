package org.project.monewping.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.notification.dto.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.mapper.NotificationMapper;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 서비스 단위 테스트")
public class NotificationServiceTest {

    private static final int DEFAULT_LIMIT = 10;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private BasicNotificationService notificationService;

    @Mock
    private NotificationMapper notificationMapper;

    private UUID userId;
    private String cursor;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cursor = Instant.now().toString();
        pageable = PageRequest.of(0, DEFAULT_LIMIT + 1);
    }

    @Test
    @DisplayName("알림이 없으면 빈 페이지 응답을 반환한다")
    void returnEmptyPageWhenNoNotifications() {
        // given
        Pageable pageable = PageRequest.of(0, DEFAULT_LIMIT + 1);
        when(notificationRepository.findPageSlice(
            eq(userId),
            eq(null),
            eq(pageable)
        ))
            .thenReturn(List.of());

        // when
        CursorPageResponseNotificationDto page = notificationService.findNotifications(
            userId,
            null,
            null,
            DEFAULT_LIMIT
        );

        // then
        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    @DisplayName("cursor가 주어지면 Instant.parse(cursor)로 findPageSlice를 호출한다")
    void useCursorWhenProvided() {
        // given
        String cursor = "2025-07-14T12:34:56Z";
        Instant parsed = Instant.parse(cursor);

        when(notificationRepository.findPageSlice(
            eq(userId),
            eq(parsed),
            eq(pageable)
        ))
            .thenReturn(List.of());

        // when
        notificationService.findNotifications(userId, cursor, null, DEFAULT_LIMIT);

        // then
        verify(notificationRepository).findPageSlice(userId, parsed, pageable);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수를 totalElements로 반환한다")
    void totalElementsReflectsConfirmedFalseCount() {
        String cursor = "2025-07-14T12:34:56Z";
        Instant parsed = Instant.parse(cursor);

        // given
        when(notificationRepository.findPageSlice(userId, parsed, pageable))
            .thenReturn(List.of());
        when(notificationRepository.countByUserIdAndConfirmedFalse(userId))
            .thenReturn(5L);

        // when
        CursorPageResponseNotificationDto page = notificationService.findNotifications(userId, cursor, null, DEFAULT_LIMIT);

        // then
        verify(notificationRepository).findPageSlice(userId, parsed, pageable);
        assertThat(page.totalElements()).isEqualTo(5L);
    }

    @Test
    @DisplayName("limit 초과 시 hasNext=true, nextCursor가 마지막 createdAt")
    void hasNextAndNextCursorForOverflow() {
        // given
        Instant base = Instant.parse(cursor);
        List<Notification> raw = IntStream.range(0, DEFAULT_LIMIT + 1)
            .mapToObj(i -> {
                Notification notification = mock(Notification.class);
                if (i == DEFAULT_LIMIT) {
                    when(notification.getCreatedAt()).thenReturn(base.plusSeconds(i));
                }
                return notification;
            })
            .toList();
        when(notificationRepository.findPageSlice(userId, base, pageable))
            .thenReturn(raw);
        when(notificationRepository.countByUserIdAndConfirmedFalse(userId))
            .thenReturn((long) raw.size());

        // when
        CursorPageResponseNotificationDto page = notificationService.findNotifications(userId, cursor, null, DEFAULT_LIMIT);

        // then
        assertThat(page.hasNext()).isTrue();
        assertThat(page.content()).hasSize(DEFAULT_LIMIT);
        String expectedCursor = base.plusSeconds(DEFAULT_LIMIT).toString();
        assertThat(page.nextCursor()).isEqualTo(expectedCursor);
    }
}