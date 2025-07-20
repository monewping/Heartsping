package org.project.monewping.domain.interest.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.exception.InterestNotFoundException;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.InterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InterestDeleteIntegrationTest {

    @Autowired
    private InterestService interestService;

    @Autowired
    private InterestRepository interestRepository;

    private Interest testInterest;
    private UUID interestId;

    @BeforeEach
    void setUp() {
        // 테스트용 관심사 생성
        testInterest = Interest.builder()
                .name("테스트 관심사")
                .subscriberCount(5L)
                .build();

        // 키워드 추가
        Keyword keyword1 = new Keyword(testInterest, "키워드1");
        Keyword keyword2 = new Keyword(testInterest, "키워드2");
        testInterest.addKeyword(keyword1);
        testInterest.addKeyword(keyword2);

        // 데이터베이스에 저장
        testInterest = interestRepository.save(testInterest);
        interestId = testInterest.getId();
    }

    @Test
    @DisplayName("관심사 삭제 시 데이터베이스에서 실제로 삭제된다")
    void should_deleteInterestFromDatabase() {
        // Given
        assertThat(interestRepository.findById(interestId)).isPresent();

        // When
        interestService.delete(interestId);

        // Then
        assertThat(interestRepository.findById(interestId)).isEmpty();
    }

    @Test
    @DisplayName("관심사 삭제 시 연관된 키워드도 함께 삭제된다")
    void should_deleteKeywordsWhenInterestDeleted() {
        // Given
        assertThat(interestRepository.findById(interestId)).isPresent();
        Interest savedInterest = interestRepository.findById(interestId).get();
        assertThat(savedInterest.getKeywords()).hasSize(2);

        // When
        interestService.delete(interestId);

        // Then
        assertThat(interestRepository.findById(interestId)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 관심사 삭제 시 예외가 발생한다")
    void should_throwException_when_deleteNonExistentInterest() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        assertThat(interestRepository.findById(nonExistentId)).isEmpty();

        // When & Then
        assertThatThrownBy(() -> interestService.delete(nonExistentId))
                .isInstanceOf(InterestNotFoundException.class)
                .hasMessageContaining("관심사를 찾을 수 없습니다: " + nonExistentId);
    }

    @Test
    @DisplayName("여러 관심사 중 특정 관심사만 삭제된다")
    void should_deleteOnlySpecificInterest() {
        // Given
        Interest anotherInterest = Interest.builder()
                .name("다른 관심사")
                .subscriberCount(3L)
                .build();
        anotherInterest = interestRepository.save(anotherInterest);
        UUID anotherId = anotherInterest.getId();

        assertThat(interestRepository.findById(interestId)).isPresent();
        assertThat(interestRepository.findById(anotherId)).isPresent();

        // When
        interestService.delete(interestId);

        // Then
        assertThat(interestRepository.findById(interestId)).isEmpty();
        assertThat(interestRepository.findById(anotherId)).isPresent();
    }

    @Test
    @DisplayName("관심사 삭제 후 동일한 이름으로 새 관심사를 생성할 수 있다")
    void should_createNewInterestWithSameNameAfterDeletion() {
        // Given
        String interestName = testInterest.getName();
        interestService.delete(interestId);
        assertThat(interestRepository.findById(interestId)).isEmpty();

        // When
        Interest newInterest = Interest.builder()
                .name(interestName)
                .subscriberCount(0L)
                .build();
        Interest savedNewInterest = interestRepository.save(newInterest);

        // Then
        assertThat(savedNewInterest.getId()).isNotEqualTo(interestId);
        assertThat(savedNewInterest.getName()).isEqualTo(interestName);
        assertThat(interestRepository.findById(savedNewInterest.getId())).isPresent();
    }

    @Test
    @DisplayName("관심사 삭제 후 전체 관심사 목록에서 제외된다")
    void should_excludeDeletedInterestFromAllInterests() {
        // Given
        Interest anotherInterest = Interest.builder()
                .name("다른 관심사")
                .subscriberCount(3L)
                .build();
        anotherInterest = interestRepository.save(anotherInterest);

        assertThat(interestRepository.findAll()).hasSize(2);

        // When
        interestService.delete(interestId);

        // Then
        List<Interest> remainingInterests = interestRepository.findAll();
        assertThat(remainingInterests).hasSize(1);
        assertThat(remainingInterests.get(0).getId()).isEqualTo(anotherInterest.getId());
    }
} 