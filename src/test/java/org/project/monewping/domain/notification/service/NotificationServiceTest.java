package org.project.monewping.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
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
import org.project.monewping.domain.notification.dto.response.CursorPageResponseNotificationDto;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.exception.InvalidCursorFormatException;
import org.project.monewping.domain.notification.exception.NotificationNotFoundException;
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
    private Instant after;
    private UUID lastId;
    private String cursor;
    private UUID notificationId;
    private UUID resourceId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        after  = Instant.parse("2025-07-22T00:00:00Z");
        lastId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        cursor = after.toString() + "|" + lastId;
    }

    @Test
    @DisplayName("첫 페이지 조회 - hasNext=true, nextCursor/Afer, size, totalElements 검증")
    void testFindNotifications_FirstPage_HasNext() {
        // given
        int limit = DEFAULT_LIMIT;
        List<Notification> slice = new ArrayList<>();
        List<NotificationDto> expectedDtos = new ArrayList<>();

        Instant lastCreatedAt = after.plusSeconds(limit - 1);
        UUID lastId = UUID.randomUUID();

        for (int i = 0; i < limit + 1; i++) {
            Notification n = mock(Notification.class);
            slice.add(n);

            if (i < limit) {
                NotificationDto dto = NotificationDto.builder()
                    .userId(userId)
                    .content("영화와 관련된 기사가 3건 등록되었습니다.")
                    .resourceId(resourceId)
                    .resourceType("Article")
                    .confirmed(false)
                    .build();
                given(notificationMapper.toDto(n)).willReturn(dto);
                expectedDtos.add(dto);
            }

            if (i == limit - 1) {
                given(n.getCreatedAt()).willReturn(lastCreatedAt);
                given(n.getId()).willReturn(lastId);
            }
        }

        given(notificationRepository.findPage(eq(userId), eq(after), isNull(), any(Pageable.class)))
            .willReturn(slice);
        given(notificationRepository.countByUserIdAndConfirmedFalse(userId))
            .willReturn(11L);

        // when
        CursorPageResponseNotificationDto result =
            notificationService.findNotifications(userId, null, after, limit);

        // then
        assertThat(result.content())
            .containsExactlyElementsOf(expectedDtos);
        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.content())
            .containsExactlyElementsOf(expectedDtos);

        String expectedCursor = lastCreatedAt.toString() + "|" + lastId;
        assertThat(result.nextCursor()).isEqualTo(expectedCursor);
        assertThat(result.nextAfter()).isEqualTo(lastCreatedAt);

        assertThat(result.size()).isEqualTo(limit);
        assertThat(result.totalElements()).isEqualTo(11L);

        then(notificationRepository)
            .should().findPage(eq(userId), eq(after), isNull(), any(Pageable.class));
        then(notificationRepository)
            .should().countByUserIdAndConfirmedFalse(userId);
    }

    @Test
    @DisplayName("두 번째 페이지 조회 - hasNext=false, 커서 파싱 후 repo 호출 검증")
    void testFindNotifications_SecondPage_NoNext() {
        // given
        int limit = DEFAULT_LIMIT;
        List<Notification> slice = new ArrayList<>();
        List<NotificationDto> expectedDtos = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            Notification n = mock(Notification.class);
            slice.add(n);

            NotificationDto dto = NotificationDto.builder()
                .userId(userId)
                .content("영화와 관련된 기사가 3건 등록되었습니다.")
                .resourceId(resourceId)
                .resourceType("Article")
                .confirmed(false)
                .build();
            given(notificationMapper.toDto(n)).willReturn(dto);
            expectedDtos.add(dto);
        }

        // repository & count 스텁
        given(notificationRepository.findPage(eq(userId), eq(after), eq(lastId), any(Pageable.class)))
            .willReturn(slice);
        given(notificationRepository.countByUserIdAndConfirmedFalse(userId))
            .willReturn(50L);

        // when
        CursorPageResponseNotificationDto result = notificationService.findNotifications(userId, cursor, null, limit);

        // then
        assertThat(result.content())
            .containsExactlyElementsOf(expectedDtos);
        assertThat(result.hasNext())
            .isFalse();
        assertThat(result.nextCursor())
            .isNull();
        assertThat(result.nextAfter())
            .isNull();
        assertThat(result.size())
            .isEqualTo(limit);
        assertThat(result.totalElements())
            .isEqualTo(50L);

        then(notificationRepository)
            .should().findPage(eq(userId), eq(after), eq(lastId), any(Pageable.class));
        then(notificationRepository)
            .should().countByUserIdAndConfirmedFalse(userId);
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
    @DisplayName("단일 알림 수정 중 존재하지 않는 유저인 경우 UserNotFoundException을 던진다")
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

    @Test
    @DisplayName("전체 알림 수정 중 존재하는 사용자에 대해 전체 알림 확인 처리 성공")
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
}