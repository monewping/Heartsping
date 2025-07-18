package org.project.monewping.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.project.monewping.domain.notification.exception.InvalidCursorFormatException;
import org.project.monewping.domain.notification.exception.NotificationNotFoundException;
import org.project.monewping.domain.notification.exception.UnsupportedResourceTypeException;
import org.project.monewping.domain.notification.mapper.NotificationMapper;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.exception.UserNotFoundException;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service 단위 테스트")
public class NotificationServiceTest {

    private static final int DEFAULT_LIMIT = 10;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BasicNotificationService notificationService;


    @Mock
    private NotificationMapper notificationMapper;

    private UUID userId;
    private String cursor;
    private UUID notificationId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
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
        assertThat(page.content())
            .as("알림이 없으면 content는 빈 리스트여야 한다")
            .isEmpty();
        assertThat(page.totalElements())
            .as("읽지 않은 알림 수가 없으므로 totalElements는 0이어야 한다")
            .isZero();
        assertThat(page.hasNext())
            .as("추가 페이지가 없으므로 hasNext는 false여야 한다")
            .isFalse();
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
        assertThat(page.totalElements())
            .as("확인되지 않은 알림이 5개이므로 totalElements는 5여야 한다")
            .isEqualTo(5L);
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
        assertThat(page.hasNext())
            .as("limit를 초과했으므로 hasNext는 true여야 한다")
            .isTrue();

        assertThat(page.content())
            .as("limit만큼의 알림이 있어야 하므로 content 크기는 DEFAULT_LIMIT와 같아야 한다")
            .hasSize(DEFAULT_LIMIT);

        String expectedCursor = base.plusSeconds(DEFAULT_LIMIT - 1).toString();

        assertThat(page.nextCursor())
            .as("다음 커서는 마지막 알림의 createdAt 값이어야 한다")
            .isEqualTo(expectedCursor);
    }

    @Test
    @DisplayName("정상적으로 알림을 확인 처리한다")
    void confirmNotificationSuccessfully() {
        // given
        Notification notification = mock(Notification.class);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(notificationRepository.findByIdAndUserId(notificationId, userId))
            .thenReturn(java.util.Optional.of(notification));

        // when
        notificationService.confirmNotification(userId, notificationId);

        // then
        verify(notification).confirm();
        verify(notificationRepository).findByIdAndUserId(notificationId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저인 경우 UserNotFoundException을 던진다")
    void throwExceptionWhenUserNotFound() {
        // given
        when(userRepository.existsById(userId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
            notificationService.confirmNotification(userId, notificationId)
        ).isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("userId=" + userId);
    }

    @Test
    @DisplayName("해당 유저의 알림이 존재하지 않으면 NotificationNotFoundException을 던진다")
    void throwExceptionWhenNotificationNotFound() {
        // given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(notificationRepository.findByIdAndUserId(notificationId, userId))
            .thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            notificationService.confirmNotification(userId, notificationId)
        ).isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("잘못된 커서 형식이면 InvalidCursorFormatException이 발생한다")
    void throwExceptionWhenInvalidCursor() {
        // given
        String invalidCursor = "2025_07_99_12:00";
        UUID userId = UUID.randomUUID();
        int limit = 10;

        // when & then
        assertThatThrownBy(() ->
            notificationService.findNotifications(userId, invalidCursor, null, limit)
        ).isInstanceOf(InvalidCursorFormatException.class)
            .hasMessageContaining(invalidCursor);
    }

    @DisplayName("알림 생성 시 리소스 타입이 잘못된 경우 예외가 발생한다")
    @Test
    void throwExceptionWhenResourceTypeUnsupported() {
        // given
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        String invalidResourceType = "UnknownType";

        // when & then
        assertThatThrownBy(() ->
            notificationService.create(userId, resourceId, invalidResourceType)
        ).isInstanceOf(UnsupportedResourceTypeException.class)
            .hasMessageContaining(invalidResourceType);
    }

    @Test
    @DisplayName("존재하는 사용자에 대해 전체 알림 확인 처리 성공")
    void confirmAllNotifications_success() {
        // given
        given(userRepository.existsById(userId)).willReturn(true);
        given(notificationRepository.confirmAllByUserId(userId)).willReturn(5);

        // when
        notificationService.confirmAll(userId);

        // then
        verify(userRepository).existsById(userId);
        verify(notificationRepository).confirmAllByUserId(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자일 경우 UserNotFoundException 발생")
    void confirmAllNotifications_userNotFound() {
        // given
        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> notificationService.confirmAll(userId))
            .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(userId);
        verify(notificationRepository, never()).confirmAllByUserId(any());
    }

    @Test
    void testCursorValueInsideException() {
        String invalidCursor = "bad_cursor";
        InvalidCursorFormatException ex = new InvalidCursorFormatException(invalidCursor, null);

        assertThat(ex.getCursor()).isEqualTo(invalidCursor);
    }
}