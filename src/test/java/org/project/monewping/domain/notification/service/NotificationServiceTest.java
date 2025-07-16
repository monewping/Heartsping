package org.project.monewping.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service 단위 테스트")
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
    }

    @Test
    @DisplayName("알림이 없으면 빈 페이지 응답을 반환한다")
    void returnEmptyPageWhenNoNotifications() {
        // given
        when(notificationRepository.findPageFirst(
            eq(userId),
            any(Pageable.class)
        )).thenReturn(List.of());

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
        verify(notificationRepository).findPageFirst(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("cursor가 주어지면 Instant.parse(cursor)로 findPageSlice를 호출한다")
    void useCursorWhenProvided() {
        // given
        String cursor = "2025-07-14T12:34:56Z";
        Instant parsed = Instant.parse(cursor);

        when(notificationRepository.findPageAfter(
            eq(userId),
            eq(parsed),
            any(Pageable.class)
        )).thenReturn(List.of());

        // when
        notificationService.findNotifications(userId, cursor, null, DEFAULT_LIMIT);

        // then
        verify(notificationRepository).findPageAfter(eq(userId), eq(parsed), any(Pageable.class));
    }

    @Test
    @DisplayName("읽지 않은 알림 개수를 totalElements로 반환한다")
    void totalElementsReflectsConfirmedFalseCount() {
        // given
        String cursor = "2025-07-14T12:34:56Z";
        Instant parsed = Instant.parse(cursor);

        when(notificationRepository.findPageAfter(eq(userId), eq(parsed), any(Pageable.class)))
            .thenReturn(List.of());
        when(notificationRepository.countByUserIdAndConfirmedFalse(userId))
            .thenReturn(5L);

        // when
        CursorPageResponseNotificationDto page = notificationService.findNotifications(
            userId, cursor, null, DEFAULT_LIMIT
        );

        // then
        verify(notificationRepository).findPageAfter(eq(userId), eq(parsed), any(Pageable.class));
        assertThat(page.totalElements()).isEqualTo(5L);
    }

    @Test
    @DisplayName("limit 초과 시 hasNext=true, nextCursor가 마지막 createdAt")
    void hasNextAndNextCursorForOverflow() {
        // given
        Instant base = Instant.parse(cursor);

        Notification notificationWithCursor = mock(Notification.class);
        when(notificationWithCursor.getCreatedAt())
            .thenReturn(base.plusSeconds(DEFAULT_LIMIT - 1));

        List<Notification> raw = Stream
            .concat(
                IntStream.range(0, DEFAULT_LIMIT - 1)
                    .mapToObj(i -> mock(Notification.class)),
                Stream.of(notificationWithCursor, mock(Notification.class))
            )
            .toList();

        when(notificationRepository.findPageAfter(eq(userId), eq(base), any(Pageable.class)))
            .thenReturn(raw);
        when(notificationRepository.countByUserIdAndConfirmedFalse(userId))
            .thenReturn((long) raw.size());

        // when
        CursorPageResponseNotificationDto page = notificationService
            .findNotifications(userId, cursor, null, DEFAULT_LIMIT);

        // then
        assertThat(page.hasNext()).isTrue();
        assertThat(page.content()).hasSize(DEFAULT_LIMIT);
        String expectedCursor = base.plusSeconds(DEFAULT_LIMIT - 1).toString();
        assertThat(page.nextCursor()).isEqualTo(expectedCursor);
    }
}