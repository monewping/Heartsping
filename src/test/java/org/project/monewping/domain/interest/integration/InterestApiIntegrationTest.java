package org.project.monewping.domain.interest.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.request.InterestRegisterRequest;
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
class InterestApiIntegrationTest {

    @Autowired
    InterestService interestService;

    @Autowired
    InterestRepository interestRepository;

    /* 관심사 등록 */
    @Test
    @DisplayName("관심사와 키워드가 정상적으로 저장된다")
    void should_SaveInterestWithKeywords_When_ValidRequest_Then_Success() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "여행",
                List.of("해외여행", "국내여행")
        );
        // When
        var dto = interestService.create(request);
        // Then
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
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "음악",
                List.of("K-POP")
        );
        interestService.create(request);
        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
                .isInstanceOf(DuplicateInterestNameException.class);
    }

    @Test
    @DisplayName("80% 이상 유사한 관심사명으로 등록 시 예외 발생")
    void should_ThrowException_When_SimilarInterestName_Then_Fail() {
        // Given
        InterestRegisterRequest request1 = new InterestRegisterRequest(
                "프로그래밍",
                List.of("Java", "Spring")
        );
        interestService.create(request1);
        // When & Then
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
        // Given
        InterestRegisterRequest request1 = new InterestRegisterRequest(
                "프로그래밍",
                List.of("Java", "Spring")
        );
        interestService.create(request1);
        // When
        InterestRegisterRequest request2 = new InterestRegisterRequest(
                "요리",
                List.of("한식", "양식")
        );
        var dto = interestService.create(request2);
        // Then
        assertThat(dto.name()).isEqualTo("요리");
        assertThat(dto.keywords()).containsExactlyInAnyOrder("한식", "양식");
    }

    @Test
    @DisplayName("접두사가 일치하는 관심사명은 유사도가 높게 측정된다")
    void should_ThrowException_When_PrefixMatches_Then_HighSimilarity() {
        // Given
        InterestRegisterRequest request1 = new InterestRegisterRequest(
                "자바",
                List.of("Java")
        );
        interestService.create(request1);
        // When & Then
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
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "",
                List.of("테스트")
        );
        // When & Then
        assertThatThrownBy(() -> interestService.create(request))
                .isInstanceOf(Exception.class); // 실제로는 MethodArgumentNotValidException 등 발생
    }

    @Test
    @DisplayName("키워드 없이 관심사만 등록할 수 있다")
    void should_SaveInterestWithoutKeywords_When_KeywordsNull_Then_Success() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "독서",
                null
        );
        // When
        var dto = interestService.create(request);
        // Then
        assertThat(dto.name()).isEqualTo("독서");
        assertThat(dto.keywords()).isEmpty();
        Interest saved = interestRepository.findByName("독서").orElseThrow();
        assertThat(saved.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("빈 키워드 리스트로 관심사를 등록할 수 있다")
    void should_SaveInterestWithoutKeywords_When_KeywordsEmpty_Then_Success() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "운동",
                List.of()
        );
        // When
        var dto = interestService.create(request);
        // Then
        assertThat(dto.name()).isEqualTo("운동");
        assertThat(dto.keywords()).isEmpty();
        Interest saved = interestRepository.findByName("운동").orElseThrow();
        assertThat(saved.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("키워드에 빈 문자열이 포함되어 있으면 무시된다")
    void should_IgnoreEmptyKeywordStrings_When_KeywordListContainsEmpty_Then_OnlyValidKeywordsSaved() {
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "게임",
                List.of("RPG", "", "FPS", "액션")
        );
        // When
        var dto = interestService.create(request);
        // Then
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
        // Given
        InterestRegisterRequest request = new InterestRegisterRequest(
            "게임null",
            new ArrayList<>(Arrays.asList("RPG", null, "FPS", "", "액션", null))
        );
        // When
        var dto = interestService.create(request);
        // Then
        Interest saved = interestRepository.findByName("게임null").orElseThrow();
        assertThat(saved.getKeywords()).isNotNull();
        assertThat(saved.getKeywords()).noneMatch(keyword -> keyword == null);
        assertThat(saved.getKeywords())
            .extracting(Keyword::getName)
            .containsExactlyInAnyOrder("RPG", "FPS", "액션");
    }

    /* 관심사 목록 조회 */
    @Test
    @DisplayName("관심사 목록을 정상적으로 조회할 수 있다 (Happy Path, 관계 매핑 포함)")
    void should_FindInterestList_When_ValidRequest_Then_Success() {
        // Given
        InterestRegisterRequest req1 = new InterestRegisterRequest("축구", List.of("공", "스포츠"));
        InterestRegisterRequest req2 = new InterestRegisterRequest("야구", List.of("방망이", "스포츠"));
        InterestRegisterRequest req3 = new InterestRegisterRequest("농구", List.of("공", "스포츠"));
        interestService.create(req1);
        interestService.create(req2);
        interestService.create(req3);

        // When
        var searchRequest = new CursorPageRequestSearchInterestDto(
                null, "name", "ASC", null, null, 10
        );
        var result = interestService.findInterestByNameAndSubcriberCountByCursor(searchRequest, "test-user");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(3);
        assertThat(result.content()).extracting("name")
                .containsExactlyInAnyOrder("축구", "야구", "농구");
        // 실제 DB에 키워드, 관계 매핑이 잘 저장됐는지 확인
        Interest soccer = interestRepository.findByName("축구").orElseThrow();
        assertThat(soccer.getKeywords()).extracting("name").contains("공", "스포츠");
    }

    @Test
    @DisplayName("검색어로 관심사 목록을 조회하면 해당 키워드/이름이 포함된 관심사만 반환된다")
    void should_FindInterestList_ByKeywordOrName() {
        // Given
        interestService.create(new InterestRegisterRequest("축구", List.of("공", "스포츠")));
        interestService.create(new InterestRegisterRequest("야구", List.of("방망이", "스포츠")));
        interestService.create(new InterestRegisterRequest("농구", List.of("공", "스포츠")));
        interestService.create(new InterestRegisterRequest("테니스", List.of("라켓", "스포츠")));

        // When: 이름에 '구'가 포함된 관심사 검색
        var searchByName = new CursorPageRequestSearchInterestDto("구", "name", "ASC", null, null, 10);
        var resultByName = interestService.findInterestByNameAndSubcriberCountByCursor(searchByName, "test-user");
        // Then
        assertThat(resultByName.content()).extracting("name")
                .containsExactlyInAnyOrder("축구", "야구", "농구");

        // When: 키워드에 '라켓'이 포함된 관심사 검색
        var searchByKeyword = new CursorPageRequestSearchInterestDto("라켓", "name", "ASC", null, null, 10);
        var resultByKeyword = interestService.findInterestByNameAndSubcriberCountByCursor(searchByKeyword, "test-user");
        // Then
        assertThat(resultByKeyword.content()).extracting("name")
                .containsExactly("테니스");
    }

    @Test
    @DisplayName("구독자 수로 오름차순/내림차순 정렬 시 정상적으로 정렬된 목록이 반환된다")
    void should_SortInterestList_BySubscriberCount_ASC_DESC() {
        // Given
        var i1 = interestService.create(new InterestRegisterRequest("축구", List.of("공")));
        var i2 = interestService.create(new InterestRegisterRequest("야구", List.of("방망이")));
        var i3 = interestService.create(new InterestRegisterRequest("농구", List.of("공")));
        // DB 직접 조작으로 구독자 수 임의 변경 (테스트 목적)
        Interest e1 = interestRepository.findByName("축구").orElseThrow();
        Interest e2 = interestRepository.findByName("야구").orElseThrow();
        Interest e3 = interestRepository.findByName("농구").orElseThrow();
        e1.increaseSubscriber(); // 1
        e1.increaseSubscriber(); // 2
        e2.increaseSubscriber(); // 1
        interestRepository.saveAll(List.of(e1, e2, e3));

        // When: 구독자 수 오름차순
        var ascRequest = new CursorPageRequestSearchInterestDto(null, "subscriberCount", "ASC", null, null, 10);
        var ascResult = interestService.findInterestByNameAndSubcriberCountByCursor(ascRequest, "test-user");
        // Then
        assertThat(ascResult.content()).extracting("name")
                .containsExactly("농구", "야구", "축구");

        // When: 구독자 수 내림차순
        var descRequest = new CursorPageRequestSearchInterestDto(null, "subscriberCount", "DESC", null, null, 10);
        var descResult = interestService.findInterestByNameAndSubcriberCountByCursor(descRequest, "test-user");
        // Then
        assertThat(descResult.content()).extracting("name")
                .containsExactly("축구", "야구", "농구");
    }

    @Test
    @DisplayName("관심사 이름으로 오름차순/내림차순 정렬 시 정상적으로 정렬된 목록이 반환된다")
    void should_SortInterestList_ByName_ASC_DESC() {
        // Given
        interestService.create(new InterestRegisterRequest("축구", List.of("공")));
        interestService.create(new InterestRegisterRequest("야구", List.of("방망이")));
        interestService.create(new InterestRegisterRequest("농구", List.of("공")));

        // When: 이름 오름차순
        var ascRequest = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 10);
        var ascResult = interestService.findInterestByNameAndSubcriberCountByCursor(ascRequest, "test-user");
        // Then
        assertThat(ascResult.content()).extracting("name")
                .containsExactly("농구", "야구", "축구");

        // When: 이름 내림차순
        var descRequest = new CursorPageRequestSearchInterestDto(null, "name", "DESC", null, null, 10);
        var descResult = interestService.findInterestByNameAndSubcriberCountByCursor(descRequest, "test-user");
        // Then
        assertThat(descResult.content()).extracting("name")
                .containsExactly("축구", "야구", "농구");
    }
}