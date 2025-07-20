package org.project.monewping.domain.useractivity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;
import org.project.monewping.domain.useractivity.exception.UserActivityNotFoundException;
import org.project.monewping.domain.useractivity.mapper.UserActivityMapper;
import org.project.monewping.domain.useractivity.repository.UserActivityRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserActivityServiceTest {

    @Mock
    private UserActivityRepository userActivityRepository;
    @Mock
    private UserActivityMapper userActivityMapper;
    @InjectMocks
    private UserActivityServiceImpl userActivityService;

    private final UUID userId = UUID.randomUUID();
    private final String email = "test@example.com";
    private final String nickname = "테스터";
    private final Instant createdAt = Instant.now();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자 활동 내역 조회 성공")
    void getUserActivity_success() {
        // given
        UserActivityDocument document = UserActivityDocument.builder()
                .userId(userId)
                .user(UserActivityDocument.UserInfo.builder()
                        .id(userId)
                        .email(email)
                        .nickname(nickname)
                        .createdAt(createdAt)
                        .build())
                .updatedAt(Instant.now())
                .build();
        UserActivityDto dto = UserActivityDto.builder().build();
        when(userActivityRepository.findByUserId(userId)).thenReturn(Optional.of(document));
        when(userActivityMapper.toDto(document)).thenReturn(dto);

        // when
        UserActivityDto result = userActivityService.getUserActivity(userId);

        // then
        assertThat(result).isNotNull();
        verify(userActivityRepository).findByUserId(userId);
        verify(userActivityMapper).toDto(document);
    }

    @Test
    @DisplayName("사용자 활동 내역 조회 실패 - 예외 발생")
    void getUserActivity_notFound() {
        // given
        when(userActivityRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userActivityService.getUserActivity(userId))
                .isInstanceOf(UserActivityNotFoundException.class);
    }

    @Test
    @DisplayName("활동 내역 초기화 - 이미 존재하면 아무 동작 안함")
    void initializeUserActivity_alreadyExists() {
        // given
        when(userActivityRepository.existsByUserId(userId)).thenReturn(true);

        // when
        userActivityService.initializeUserActivity(userId, email, nickname, createdAt);

        // then
        verify(userActivityRepository, never()).save(any());
    }

    @Test
    @DisplayName("활동 내역 초기화 - 새로 생성")
    void initializeUserActivity_new() {
        // given
        when(userActivityRepository.existsByUserId(userId)).thenReturn(false);

        // when
        userActivityService.initializeUserActivity(userId, email, nickname, createdAt);

        // then
        verify(userActivityRepository).save(any(UserActivityDocument.class));
    }

    @Test
    @DisplayName("활동 내역 삭제")
    void deleteUserActivity() {
        // when
        userActivityService.deleteUserActivity(userId);
        // then
        verify(userActivityRepository).deleteByUserId(userId);
    }
}