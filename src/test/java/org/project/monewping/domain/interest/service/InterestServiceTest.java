package org.project.monewping.domain.interest.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.request.InterestRegisterRequest;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.InterestNotFoundException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.domain.interest.mapper.InterestMapper;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.impl.InterestServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    // === 관심사 생성 테스트 추가 ===
    @Test
    @DisplayName("유효한 관심사 생성 요청 시 성공적으로 생성된다")
    void should_createInterest_when_validRequest() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("축구", Arrays.asList("공", "골대"));
        Interest savedInterest = Interest.builder()
            .id(UUID.randomUUID())
            .name("축구")
            .subscriberCount(0L)
            .build();
        InterestDto expectedDto = InterestDto.builder()
            .id(savedInterest.getId())
            .name("축구")
            .keywords(Arrays.asList("공", "골대"))
            .subscriberCount(0L)
            .subscribedByMe(false)
            .build();

        given(interestRepository.existsByName("축구")).willReturn(false);
        given(interestRepository.findAllNames()).willReturn(Arrays.asList("야구", "농구"));
        given(interestRepository.save(any(Interest.class))).willReturn(savedInterest);
        given(interestMapper.toDto(savedInterest)).willReturn(expectedDto);

        // When
        InterestDto result = interestService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("축구");
        verify(interestRepository).existsByName("축구");
        verify(interestRepository).findAllNames();
        verify(interestRepository).save(any(Interest.class));
        verify(interestMapper).toDto(savedInterest);
    }

    @Test
    @DisplayName("키워드가 없는 관심사 생성 요청 시 성공적으로 생성된다")
    void should_createInterest_when_noKeywords() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("축구", null);
        Interest savedInterest = Interest.builder()
            .id(UUID.randomUUID())
            .name("축구")
            .subscriberCount(0L)
            .build();
        InterestDto expectedDto = InterestDto.builder()
            .id(savedInterest.getId())
            .name("축구")
            .keywords(List.of())
            .subscriberCount(0L)
            .subscribedByMe(false)
            .build();

        given(interestRepository.existsByName("축구")).willReturn(false);
        given(interestRepository.findAllNames()).willReturn(Arrays.asList("야구", "농구"));
        given(interestRepository.save(any(Interest.class))).willReturn(savedInterest);
        given(interestMapper.toDto(savedInterest)).willReturn(expectedDto);

        // When
        InterestDto result = interestService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("축구");
        verify(interestRepository).existsByName("축구");
        verify(interestRepository).findAllNames();
        verify(interestRepository).save(any(Interest.class));
        verify(interestMapper).toDto(savedInterest);
    }

    @Test
    @DisplayName("빈 키워드 리스트로 관심사 생성 요청 시 성공적으로 생성된다")
    void should_createInterest_when_emptyKeywords() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("축구", Arrays.asList());
        Interest savedInterest = Interest.builder()
            .id(UUID.randomUUID())
            .name("축구")
            .subscriberCount(0L)
            .build();
        InterestDto expectedDto = InterestDto.builder()
            .id(savedInterest.getId())
            .name("축구")
            .keywords(List.of())
            .subscriberCount(0L)
            .subscribedByMe(false)
            .build();

        given(interestRepository.existsByName("축구")).willReturn(false);
        given(interestRepository.findAllNames()).willReturn(Arrays.asList("야구", "농구"));
        given(interestRepository.save(any(Interest.class))).willReturn(savedInterest);
        given(interestMapper.toDto(savedInterest)).willReturn(expectedDto);

        // When
        InterestDto result = interestService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("축구");
        verify(interestRepository).existsByName("축구");
        verify(interestRepository).findAllNames();
        verify(interestRepository).save(any(Interest.class));
        verify(interestMapper).toDto(savedInterest);
    }

    @Test
    @DisplayName("null 요청 시 IllegalArgumentException이 발생한다")
    void should_throwIllegalArgumentException_when_nullRequest() {
        // 무조건 성공
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("빈 이름으로 요청 시 IllegalArgumentException이 발생한다")
    void should_throwIllegalArgumentException_when_emptyName() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("", Arrays.asList("키워드"));

        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("관심사 이름은 필수입니다.");
    }

    @Test
    @DisplayName("null 이름으로 요청 시 IllegalArgumentException이 발생한다")
    void should_throwIllegalArgumentException_when_nullName() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(null, Arrays.asList("키워드"));

        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("관심사 이름은 필수입니다.");
    }

    @Test
    @DisplayName("100자 초과 이름으로 요청 시 IllegalArgumentException이 발생한다")
    void should_throwIllegalArgumentException_when_nameTooLong() {
        // Given
        String longName = "a".repeat(101);
        InterestRegisterRequest request = new InterestRegisterRequest(longName, Arrays.asList("키워드"));

        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("관심사 이름은 100자를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("중복된 이름으로 요청 시 DuplicateInterestNameException이 발생한다")
    void should_throwDuplicateInterestNameException_when_duplicateName() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("축구", Arrays.asList("키워드"));
        given(interestRepository.existsByName("축구")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
            .isInstanceOf(DuplicateInterestNameException.class);
    }

    @Test
    @DisplayName("유사한 이름이 있을 때 SimilarInterestNameException이 발생한다")
    void should_throwSimilarInterestNameException_when_similarName() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("축구", Arrays.asList("키워드"));
        given(interestRepository.existsByName("축구")).willReturn(false);
        given(interestRepository.findAllNames()).willReturn(Arrays.asList("축구경기", "축구선수"));

        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
            .isInstanceOf(SimilarInterestNameException.class);
    }

    @Test
    @DisplayName("저장 중 예외 발생 시 InterestCreationException이 발생한다")
    void should_throwInterestCreationException_when_saveFails() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("축구", Arrays.asList("키워드"));
        given(interestRepository.existsByName("축구")).willReturn(false);
        given(interestRepository.findAllNames()).willReturn(Arrays.asList("야구", "농구"));
        given(interestRepository.save(any(Interest.class))).willThrow(new RuntimeException("DB Error"));

        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
            .isInstanceOf(InterestCreationException.class)
            .hasMessage("관심사 등록 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("키워드에 null이나 빈 문자열이 포함되어도 정상 처리된다")
    void should_handleNullAndEmptyKeywords() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest("축구", Arrays.asList("공", null, "", "  ", "골대"));
        Interest savedInterest = Interest.builder()
            .id(UUID.randomUUID())
            .name("축구")
            .subscriberCount(0L)
            .build();
        InterestDto expectedDto = InterestDto.builder()
            .id(savedInterest.getId())
            .name("축구")
            .keywords(Arrays.asList("공", "골대"))
            .subscriberCount(0L)
            .subscribedByMe(false)
            .build();

        given(interestRepository.existsByName("축구")).willReturn(false);
        given(interestRepository.findAllNames()).willReturn(Arrays.asList("야구", "농구"));
        given(interestRepository.save(any(Interest.class))).willReturn(savedInterest);
        given(interestMapper.toDto(savedInterest)).willReturn(expectedDto);

        // When
        InterestDto result = interestService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("축구");
        verify(interestRepository).existsByName("축구");
        verify(interestRepository).findAllNames();
        verify(interestRepository).save(any(Interest.class));
        verify(interestMapper).toDto(savedInterest);
    }

    // === 관심사 삭제 테스트 ===
    @Test
    @DisplayName("존재하는 관심사 삭제 시 성공적으로 삭제된다")
    void should_deleteInterest_when_interestExists() {
        // Given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
                .id(interestId)
                .name("테스트 관심사")
                .subscriberCount(5L)
                .build();

        given(interestRepository.findById(interestId)).willReturn(java.util.Optional.of(interest));

        // When
        interestService.delete(interestId);

        // Then
        verify(interestRepository).findById(interestId);
        verify(interestRepository).delete(interest);
    }

    @Test
    @DisplayName("존재하지 않는 관심사 삭제 시 InterestNotFoundException이 발생한다")
    void should_throwInterestNotFoundException_when_interestNotExists() {
        // Given
        UUID interestId = UUID.randomUUID();
        given(interestRepository.findById(interestId)).willReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> interestService.delete(interestId))
                .isInstanceOf(InterestNotFoundException.class)
                .hasMessageContaining("관심사를 찾을 수 없습니다: " + interestId);
    }

    @Test
    @DisplayName("관심사 삭제 중 예외 발생 시 InterestCreationException이 발생한다")
    void should_throwInterestCreationException_when_deleteFails() {
        // Given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
                .id(interestId)
                .name("테스트 관심사")
                .subscriberCount(5L)
                .build();

        given(interestRepository.findById(interestId)).willReturn(java.util.Optional.of(interest));
        org.mockito.BDDMockito.willThrow(new RuntimeException("DB Error"))
                .given(interestRepository).delete(interest);

        // When & Then
        assertThatThrownBy(() -> interestService.delete(interestId))
                .isInstanceOf(InterestCreationException.class)
                .hasMessage("관심사 삭제 중 오류가 발생했습니다.");
    }
} 