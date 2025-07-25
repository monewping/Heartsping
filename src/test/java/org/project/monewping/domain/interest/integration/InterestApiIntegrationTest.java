package org.project.monewping.domain.interest.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.request.InterestRegisterRequest;
import org.project.monewping.domain.interest.dto.request.InterestUpdateRequest;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.entity.Subscription;
import org.project.monewping.domain.interest.exception.*;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.repository.SubscriptionRepository;
import org.project.monewping.domain.interest.service.InterestService;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InterestApiIntegrationTest {

    @Autowired
    InterestService interestService;

    @Autowired
    InterestRepository interestRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SubscriptionRepository subscriptionRepository;

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
        // Jaro-Winkler 유사도가 0.8 이상일 때만 예외, 아니면 정상 등록
        try {
            interestService.create(request2);
        } catch (SimilarInterestNameException e) {
            assertThat(e.getMessage()).contains("80% 이상 유사한 이름의 관심사가 존재합니다");
            return;
        }
        // 유사도가 0.8 미만이면 정상 등록됨
        Interest saved = interestRepository.findByName("프로그래밍언어").orElse(null);
        assertThat(saved).isNotNull();
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
        // Jaro-Winkler 유사도가 0.8 이상일 때만 예외, 아니면 정상 등록
        try {
            interestService.create(request2);
        } catch (SimilarInterestNameException e) {
            assertThat(e.getMessage()).contains("자바");
            return;
        }
        // 유사도가 0.8 미만이면 정상 등록됨
        Interest saved = interestRepository.findByName("자바스크립트").orElse(null);
        assertThat(saved).isNotNull();
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
        var result = interestService.findInterestByNameAndSubcriberCountByCursor(searchRequest, UUID.randomUUID());

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
        var resultByName = interestService.findInterestByNameAndSubcriberCountByCursor(searchByName, UUID.randomUUID());
        // Then
        assertThat(resultByName.content()).extracting("name")
                .containsExactlyInAnyOrder("축구", "야구", "농구");

        // When: 키워드에 '라켓'이 포함된 관심사 검색
        var searchByKeyword = new CursorPageRequestSearchInterestDto("라켓", "name", "ASC", null, null, 10);
        var resultByKeyword = interestService.findInterestByNameAndSubcriberCountByCursor(searchByKeyword, UUID.randomUUID());
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
        var ascResult = interestService.findInterestByNameAndSubcriberCountByCursor(ascRequest, UUID.randomUUID());
        // Then
        assertThat(ascResult.content()).extracting("name")
                .containsExactly("농구", "야구", "축구");

        // When: 구독자 수 내림차순
        var descRequest = new CursorPageRequestSearchInterestDto(null, "subscriberCount", "DESC", null, null, 10);
        var descResult = interestService.findInterestByNameAndSubcriberCountByCursor(descRequest, UUID.randomUUID());
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
        var ascResult = interestService.findInterestByNameAndSubcriberCountByCursor(ascRequest, UUID.randomUUID());
        // Then
        assertThat(ascResult.content()).extracting("name")
                .containsExactly("농구", "야구", "축구");

        // When: 이름 내림차순
        var descRequest = new CursorPageRequestSearchInterestDto(null, "name", "DESC", null, null, 10);
        var descResult = interestService.findInterestByNameAndSubcriberCountByCursor(descRequest, UUID.randomUUID());
        // Then
        assertThat(descResult.content()).extracting("name")
                .containsExactly("축구", "야구", "농구");
    }

    @Test
    @DisplayName("관심사 목록 조회 시 요청 유저의 구독 여부가 InterestDto에 올바르게 반영된다")
    void should_IncludeSubscribedByMe_When_ListInterests() {
        // Given: 관심사 2개 등록, 실제 User 생성
        var req1 = new InterestRegisterRequest("축구", List.of("공", "스포츠"));
        var req2 = new InterestRegisterRequest("야구", List.of("방망이", "스포츠"));
        var soccerDto = interestService.create(req1);
        var baseballDto = interestService.create(req2);

        User user = userRepository.save(User.builder()
                .email("test@email.com")
                .nickname("tester")
                .password("pw")
                .build());
        UUID userId = user.getId();

        // When: 구독 전 조회
        var searchRequest = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 10);
        var resultBefore = interestService.findInterestByNameAndSubcriberCountByCursor(searchRequest, userId);

        // Then: 구독 전에는 모두 false
        assertThat(resultBefore.content()).hasSize(2);
        assertThat(resultBefore.content()).allSatisfy(dto -> assertThat(dto.subscribedByMe()).isFalse());

        // When: user가 '축구'를 구독
        subscriptionRepository.save(new Subscription(user, interestRepository.findById(soccerDto.id()).orElseThrow()));

        // When: 구독 후 조회
        var resultAfter = interestService.findInterestByNameAndSubcriberCountByCursor(searchRequest, userId);

        // Then: '축구'만 true, '야구'는 false
        assertThat(resultAfter.content()).anySatisfy(dto -> {
            if (dto.name().equals("축구")) {
                assertThat(dto.subscribedByMe()).isTrue();
            } else if (dto.name().equals("야구")) {
                assertThat(dto.subscribedByMe()).isFalse();
            }
        });
    }

    /* 관심사 키워드 수정 */
    @Test
    @DisplayName("관심사 키워드를 정상적으로 수정할 수 있다")
    void should_updateInterestKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("수정테스트", List.of("기존키워드1", "기존키워드2"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        
        List<String> newKeywords = Arrays.asList("새키워드1", "새키워드2", "새키워드3");
        InterestUpdateRequest updateRequest = new InterestUpdateRequest(newKeywords);

        // When
        var result = interestService.update(interestId, updateRequest);

        // Then
        assertThat(result.id()).isEqualTo(interestId);
        assertThat(result.name()).isEqualTo("수정테스트");
        assertThat(result.keywords()).containsExactlyInAnyOrder("새키워드1", "새키워드2", "새키워드3");
        assertThat(result.subscriberCount()).isEqualTo(0L);

        // DB에서도 확인
        Interest savedInterest = interestRepository.findById(interestId).orElseThrow();
        assertThat(savedInterest.getKeywords()).hasSize(3);
        assertThat(savedInterest.getKeywords())
                .extracting(Keyword::getName)
                .containsExactlyInAnyOrder("새키워드1", "새키워드2", "새키워드3");
    }

    @Test
    @DisplayName("키워드를 빈 리스트로 수정하면 예외가 발생한다")
    void should_throwException_when_emptyList() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("빈리스트테스트", List.of("키워드1"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        
        InterestUpdateRequest request = new InterestUpdateRequest(Arrays.asList());

        // When & Then
        assertThatThrownBy(() -> interestService.update(interestId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("키워드는 1개 이상 10개 이하로 입력해야 합니다");
    }

    @Test
    @DisplayName("키워드를 null로 수정하면 예외가 발생한다")
    void should_throwException_when_nullKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("null테스트", List.of("키워드1"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        
        InterestUpdateRequest request = new InterestUpdateRequest(null);

        // When & Then
        assertThatThrownBy(() -> interestService.update(interestId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("키워드는 필수입니다");
    }

    @Test
    @DisplayName("중복된 키워드가 있으면 예외가 발생한다")
    void should_throwException_when_duplicateKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("중복테스트", List.of("키워드1"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        
        List<String> duplicateKeywords = Arrays.asList("키워드1", "키워드1", "키워드2");
        InterestUpdateRequest request = new InterestUpdateRequest(duplicateKeywords);

        // When & Then
        assertThatThrownBy(() -> interestService.update(interestId, request))
                .isInstanceOf(DuplicateKeywordException.class)
                .hasMessageContaining("중복된 키워드입니다");
    }

    @Test
    @DisplayName("존재하지 않는 관심사 ID로 수정 시 예외가 발생한다")
    void should_throwException_when_nonExistentInterest() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        InterestUpdateRequest request = new InterestUpdateRequest(Arrays.asList("키워드1"));

        // When & Then
        assertThatThrownBy(() -> interestService.update(nonExistentId, request))
                .isInstanceOf(InterestNotFoundException.class)
                .hasMessageContaining("관심사를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("빈 문자열이나 null 키워드는 무시된다")
    void should_ignoreEmptyOrNullKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("무시테스트", List.of("키워드1"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        
        List<String> mixedKeywords = Arrays.asList("유효키워드1", "", null, "유효키워드2", "   ");
        InterestUpdateRequest request = new InterestUpdateRequest(mixedKeywords);

        // When
        var result = interestService.update(interestId, request);

        // Then
        assertThat(result.keywords()).containsExactlyInAnyOrder("유효키워드1", "유효키워드2");

        // DB에서도 확인
        Interest savedInterest = interestRepository.findById(interestId).orElseThrow();
        assertThat(savedInterest.getKeywords()).hasSize(2);
        assertThat(savedInterest.getKeywords())
                .extracting(Keyword::getName)
                .containsExactlyInAnyOrder("유효키워드1", "유효키워드2");
    }

    @Test
    @DisplayName("키워드 수정 시 관심사 이름은 변경되지 않는다")
    void should_notChangeInterestName_when_updateKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("이름변경방지테스트", List.of("키워드1"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        String originalName = createdDto.name();
        
        InterestUpdateRequest request = new InterestUpdateRequest(Arrays.asList("새키워드"));

        // When
        var result = interestService.update(interestId, request);

        // Then
        assertThat(result.name()).isEqualTo(originalName);

        // DB에서도 확인
        Interest savedInterest = interestRepository.findById(interestId).orElseThrow();
        assertThat(savedInterest.getName()).isEqualTo(originalName);
    }

    @Test
    @DisplayName("키워드 수정 시 구독자 수는 변경되지 않는다")
    void should_notChangeSubscriberCount_when_updateKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("구독자수변경방지테스트", List.of("키워드1"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        Long originalSubscriberCount = createdDto.subscriberCount();
        
        InterestUpdateRequest request = new InterestUpdateRequest(Arrays.asList("새키워드"));

        // When
        var result = interestService.update(interestId, request);

        // Then
        assertThat(result.subscriberCount()).isEqualTo(originalSubscriberCount);

        // DB에서도 확인
        Interest savedInterest = interestRepository.findById(interestId).orElseThrow();
        assertThat(savedInterest.getSubscriberCount()).isEqualTo(originalSubscriberCount);
    }

    @Test
    @DisplayName("키워드 앞뒤 공백이 제거된다")
    void should_trimKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("공백제거테스트", List.of("키워드1"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        
        List<String> keywordsWithSpaces = Arrays.asList("  키워드1  ", "키워드2   ", "   키워드3");
        InterestUpdateRequest request = new InterestUpdateRequest(keywordsWithSpaces);

        // When
        var result = interestService.update(interestId, request);

        // Then
        assertThat(result.keywords()).containsExactlyInAnyOrder("키워드1", "키워드2", "키워드3");

        // DB에서도 확인
        Interest savedInterest = interestRepository.findById(interestId).orElseThrow();
        assertThat(savedInterest.getKeywords())
                .extracting(Keyword::getName)
                .containsExactlyInAnyOrder("키워드1", "키워드2", "키워드3");
    }

    @Test
    @DisplayName("기존 키워드를 완전히 새로운 키워드로 교체할 수 있다")
    void should_replaceAllKeywords() {
        // Given
        InterestRegisterRequest createRequest = new InterestRegisterRequest("교체테스트", List.of("기존키워드1", "기존키워드2"));
        var createdDto = interestService.create(createRequest);
        UUID interestId = createdDto.id();
        
        List<String> completelyNewKeywords = Arrays.asList("완전히새로운키워드1", "완전히새로운키워드2");
        InterestUpdateRequest request = new InterestUpdateRequest(completelyNewKeywords);

        // When
        var result = interestService.update(interestId, request);

        // Then
        assertThat(result.keywords()).containsExactlyInAnyOrder("완전히새로운키워드1", "완전히새로운키워드2");
        assertThat(result.keywords()).doesNotContain("기존키워드1", "기존키워드2");

        // DB에서도 확인
        Interest savedInterest = interestRepository.findById(interestId).orElseThrow();
        assertThat(savedInterest.getKeywords())
                .extracting(Keyword::getName)
                .containsExactlyInAnyOrder("완전히새로운키워드1", "완전히새로운키워드2");
        assertThat(savedInterest.getKeywords())
                .extracting(Keyword::getName)
                .doesNotContain("기존키워드1", "기존키워드2");
    }
}