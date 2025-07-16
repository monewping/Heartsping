package org.project.monewping.domain.interest.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.mapper.InterestMapper;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.impl.InterestServiceImpl;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @InjectMocks
    private InterestServiceImpl interestService;

    @Mock
    private InterestRepository interestRepository;
    @Mock
    private InterestMapper interestMapper;

    @Test
    @DisplayName("관심사 목록 조회 요청 시 Repository에서 결과를 받아 반환한다")
    void should_returnResult_when_findInterestByNameAndSubcriberCountByCursor() {
        // Given
        given(interestRepository.searchWithCursor(any(), any(UUID.class)))
                .willReturn(new CursorPageResponseInterestDto(null, null, null, 0, 0L, false));

        // When
        CursorPageResponseInterestDto result = interestService.findInterestByNameAndSubcriberCountByCursor(
                new CursorPageRequestSearchInterestDto("test", "name", "ASC", null, null, 10),
                UUID.randomUUID()
        );

        // Then
        assertThat(result).isNotNull();
        verify(interestRepository, times(1)).searchWithCursor(any(), any(UUID.class));
    }

    @Test
    @DisplayName("Repository에서 예외 발생 시 Service도 예외를 던진다")
    void should_throwException_when_repositoryThrows() {
        // Given
        given(interestRepository.searchWithCursor(any(), any(UUID.class)))
                .willThrow(new RuntimeException("DB Error"));

        // When & Then
        assertThatThrownBy(() -> interestService.findInterestByNameAndSubcriberCountByCursor(
                new CursorPageRequestSearchInterestDto("test", "name", "ASC", null, null, 10),
                UUID.randomUUID()
        )).isInstanceOf(RuntimeException.class)
          .hasMessageContaining("DB Error");
    }

    @Test
    @DisplayName("Repository에서 null 반환 시 Service도 null을 반환한다")
    void should_returnNull_when_repositoryReturnsNull() {
        // Given
        given(interestRepository.searchWithCursor(any(), any(UUID.class)))
                .willReturn(null);

        // When
        CursorPageResponseInterestDto result = interestService.findInterestByNameAndSubcriberCountByCursor(
                new CursorPageRequestSearchInterestDto("test", "name", "ASC", null, null, 10),
                UUID.randomUUID()
        );

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("검색어, 정렬, 커서 등 다양한 파라미터로 목록 조회 위임이 잘 동작한다")
    void should_delegateToRepository_withVariousParams() {
        // Given
        CursorPageRequestSearchInterestDto request = new CursorPageRequestSearchInterestDto(
                "축구", "subscriberCount", "DESC", "some-cursor", "2024-07-15T10:00:00Z", 20
        );
        CursorPageResponseInterestDto mockResponse = new CursorPageResponseInterestDto(null, null, null, 0, 0L, false);
        UUID userId = UUID.randomUUID();
        given(interestRepository.searchWithCursor(request, userId)).willReturn(mockResponse);

        // When
        CursorPageResponseInterestDto result = interestService.findInterestByNameAndSubcriberCountByCursor(request, userId);

        // Then
        assertThat(result).isNotNull();
        verify(interestRepository).searchWithCursor(request, userId);
    }

    @Test
    @DisplayName("파라미터가 null이거나 최소/최대값일 때도 정상 동작한다")
    void should_delegateToRepository_withEdgeCaseParams() {
        // Given
        CursorPageRequestSearchInterestDto request1 = new CursorPageRequestSearchInterestDto(null, null, null, null, null, 1);
        CursorPageRequestSearchInterestDto request2 = new CursorPageRequestSearchInterestDto("", "name", "ASC", null, null, 100);
        CursorPageResponseInterestDto mockResponse = new CursorPageResponseInterestDto(null, null, null, 0, 0L, false);
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        given(interestRepository.searchWithCursor(any(), any(UUID.class))).willReturn(mockResponse);

        // When
        CursorPageResponseInterestDto result1 = interestService.findInterestByNameAndSubcriberCountByCursor(request1, userId1);
        CursorPageResponseInterestDto result2 = interestService.findInterestByNameAndSubcriberCountByCursor(request2, userId2);

        // Then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        verify(interestRepository).searchWithCursor(request1, userId1);
        verify(interestRepository).searchWithCursor(request2, userId2);
    }
} 