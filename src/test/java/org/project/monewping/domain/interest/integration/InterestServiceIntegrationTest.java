package org.project.monewping.domain.interest.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.InterestRegisterRequest;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.InterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InterestServiceIntegrationTest {

    @Autowired
    InterestService interestService;

    @Autowired
    InterestRepository interestRepository;

    @Test
    @DisplayName("관심사와 키워드가 정상적으로 저장된다")
    void should_SaveInterestWithKeywords_When_ValidRequest_Then_Success() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "여행",
                List.of("해외여행", "국내여행")
        );
        // when
        var dto = interestService.create(request);
        // then
        Interest saved = interestRepository.findByName("여행").orElseThrow();
        assertThat(saved.getName()).isEqualTo("여행");
        assertThat(saved.getKeywords()).hasSize(2);
        assertThat(saved.getKeywords())
                .extracting(Keyword::getName)
                .containsExactlyInAnyOrder("해외여행", "국내여행");
    }

    @Test
    @DisplayName("중복된 관심사명으로 등록 시 예외 발생")
    void should_ThrowException_When_DuplicateInterestName_Then_Fail() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "음악",
                List.of("K-POP")
        );
        interestService.create(request);
        // when & then
        assertThatThrownBy(() -> interestService.create(request))
                .isInstanceOf(DuplicateInterestNameException.class);
    }

    @Test
    @DisplayName("80% 이상 유사한 관심사명으로 등록 시 예외 발생")
    void should_ThrowException_When_SimilarInterestName_Then_Fail() {
        // given
        InterestRegisterRequest request1 = new InterestRegisterRequest(
                "프로그래밍",
                List.of("Java", "Spring")
        );
        interestService.create(request1);
        // when & then
        InterestRegisterRequest request2 = new InterestRegisterRequest(
                "프로그래밍언어",
                List.of("Python", "JavaScript")
        );
        assertThatThrownBy(() -> interestService.create(request2))
                .isInstanceOf(SimilarInterestNameException.class)
                .hasMessageContaining("80% 이상 유사한 이름의 관심사가 존재합니다");
    }

    @Test
    @DisplayName("유사하지 않은 관심사명은 정상 등록된다")
    void should_SaveInterest_When_DifferentName_Then_Success() {
        // given
        InterestRegisterRequest request1 = new InterestRegisterRequest(
                "프로그래밍",
                List.of("Java", "Spring")
        );
        interestService.create(request1);
        // when
        InterestRegisterRequest request2 = new InterestRegisterRequest(
                "요리",
                List.of("한식", "양식")
        );
        var dto = interestService.create(request2);
        // then
        assertThat(dto.name()).isEqualTo("요리");
        assertThat(dto.keywords()).containsExactlyInAnyOrder("한식", "양식");
    }

    @Test
    @DisplayName("접두사가 일치하는 관심사명은 유사도가 높게 측정된다")
    void should_ThrowException_When_PrefixMatches_Then_HighSimilarity() {
        // given
        InterestRegisterRequest request1 = new InterestRegisterRequest(
                "자바",
                List.of("Java")
        );
        interestService.create(request1);
        // when & then
        InterestRegisterRequest request2 = new InterestRegisterRequest(
                "자바스크립트",
                List.of("JavaScript")
        );
        assertThatThrownBy(() -> interestService.create(request2))
                .isInstanceOf(SimilarInterestNameException.class)
                .hasMessageContaining("자바스크립트")
                .hasMessageContaining("자바");
    }

    @Test
    @DisplayName("유효하지 않은 관심사명(빈 값) 등록 시 예외 발생")
    void should_ThrowException_When_EmptyName_Then_Fail() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "",
                List.of("테스트")
        );
        // when & then
        assertThatThrownBy(() -> interestService.create(request))
                .isInstanceOf(Exception.class); // 실제로는 MethodArgumentNotValidException 등 발생
    }

    @Test
    @DisplayName("키워드 없이 관심사만 등록할 수 있다")
    void should_SaveInterestWithoutKeywords_When_KeywordsNull_Then_Success() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "독서",
                null
        );
        // when
        var dto = interestService.create(request);
        // then
        assertThat(dto.name()).isEqualTo("독서");
        assertThat(dto.keywords()).isEmpty();
        Interest saved = interestRepository.findByName("독서").orElseThrow();
        assertThat(saved.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("빈 키워드 리스트로 관심사를 등록할 수 있다")
    void should_SaveInterestWithoutKeywords_When_KeywordsEmpty_Then_Success() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "운동",
                List.of()
        );
        // when
        var dto = interestService.create(request);
        // then
        assertThat(dto.name()).isEqualTo("운동");
        assertThat(dto.keywords()).isEmpty();
        Interest saved = interestRepository.findByName("운동").orElseThrow();
        assertThat(saved.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("키워드에 빈 문자열이 포함되어 있으면 무시된다")
    void should_IgnoreEmptyKeywordStrings_When_KeywordListContainsEmpty_Then_OnlyValidKeywordsSaved() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "게임",
                List.of("RPG", "", "FPS", "액션")
        );
        // when
        var dto = interestService.create(request);
        // then
        assertThat(dto.keywords()).containsExactlyInAnyOrder("RPG", "FPS", "액션");
        Interest saved = interestRepository.findByName("게임").orElseThrow();
        assertThat(saved.getKeywords()).hasSize(3);
        assertThat(saved.getKeywords())
                .extracting(Keyword::getName)
                .containsExactlyInAnyOrder("RPG", "FPS", "액션");
    }

    @Test
    @DisplayName("키워드 리스트에 null이 포함되어도 DB에는 저장되지 않는다")
    void should_IgnoreNullKeywords_When_KeywordListContainsNull_Then_OnlyValidKeywordsSaved() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
            "게임null",
            new ArrayList<>(Arrays.asList("RPG", null, "FPS", "", "액션", null))
        );
        // when
        var dto = interestService.create(request);
        // then
        Interest saved = interestRepository.findByName("게임null").orElseThrow();
        assertThat(saved.getKeywords()).isNotNull();
        assertThat(saved.getKeywords()).noneMatch(keyword -> keyword == null);
        assertThat(saved.getKeywords())
            .extracting(Keyword::getName)
            .containsExactlyInAnyOrder("RPG", "FPS", "액션");
    }
}