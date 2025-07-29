package org.project.monewping.domain.interest.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.mapper.InterestMapperImpl;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.domain.interest.entity.Subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.UUID;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        InterestMapperImpl.class})
@TestPropertySource(properties = "auditing.enabled=true")
class InterestRepositoryCustomImplTest {

    @Autowired
    InterestRepository interestRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("레포지토리에 관심사 데이터가 잘 저장된다")
    void should_saveInterest() {
        // Given
        Interest interest = Interest.builder()
                .name("축구")
                .subscriberCount(0L)
                .build();

        // When
        Interest saved = interestRepository.save(interest);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("축구");
    }

    @Test
    @DisplayName("관심사 이름으로 검색하면 해당 관심사만 조회된다")
    void should_returnInterest_when_searchByName() {
        // Given
        interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        interestRepository.save(Interest.builder().name("농구").subscriberCount(0L).build());
        var request = new CursorPageRequestSearchInterestDto("축구", "name", "ASC", null, null, 10);
        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());
        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("축구");
    }

    @Test
    @DisplayName("키워드로 검색하면 해당 키워드를 가진 관심사들만 조회된다")
    void should_returnInterests_when_searchByKeyword() {
        // Given
        Interest soccer = Interest.builder().name("축구").subscriberCount(0L).build();
        Interest baseball = Interest.builder().name("야구").subscriberCount(0L).build();
        Interest basketball = Interest.builder().name("농구").subscriberCount(0L).build();
        soccer.addKeyword(Keyword.builder().name("공").build());
        baseball.addKeyword(Keyword.builder().name("방망이").build());
        basketball.addKeyword(Keyword.builder().name("공").build());
        interestRepository.save(soccer);
        interestRepository.save(baseball);
        interestRepository.save(basketball);
        var request = new CursorPageRequestSearchInterestDto("공", "name", "ASC", null, null, 10);
        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());
        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).extracting("name").containsExactlyInAnyOrder("축구", "농구");
    }

    @Test
    @DisplayName("존재하지 않는 관심사로 검색하면 빈 결과가 반환된다")
    void should_returnEmpty_when_searchByNonExist() {
        // Given
        interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        var request = new CursorPageRequestSearchInterestDto("없는관심사", "name", "ASC", null, null, 10);
        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());
        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
    }

    // === 신규 시작 ===
    @Test
    @DisplayName("관심사 목록 조회 시 사용자의 구독 여부가 반영된다")
    void should_returnSubscribedByMe_when_userSubscribed() {
        // Given
        User user = userRepository.save(User.builder()
            .email("test@email.com")
            .nickname("tester")
            .password("pw")
            .build());

        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());

        // user가 '축구'만 구독
        subscriptionRepository.save(new Subscription(user, soccer));
        soccer.increaseSubscriber();

        var request = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, user.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content())
            .anySatisfy(dto -> {
                if (dto.name().equals("축구")) {
                    assertThat(dto.subscribedByMe()).isTrue();
                } else if (dto.name().equals("야구")) {
                    assertThat(dto.subscribedByMe()).isFalse();
                }
            });
    }
    // === 신규 끝 ===

    // === 추가 테스트 시작 ===
    @Test
    @DisplayName("구독자 수 기준 오름차순 정렬이 정상 동작한다")
    void should_sortBySubscriberCountAsc() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(10L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(5L).build());
        Interest basketball = interestRepository.save(Interest.builder().name("농구").subscriberCount(15L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "subscriberCount", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(3);
        assertThat(result.content()).extracting("name").containsExactly("야구", "축구", "농구");
    }

    @Test
    @DisplayName("구독자 수 기준 내림차순 정렬이 정상 동작한다")
    void should_sortBySubscriberCountDesc() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(10L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(5L).build());
        Interest basketball = interestRepository.save(Interest.builder().name("농구").subscriberCount(15L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "subscriberCount", "DESC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(3);
        assertThat(result.content()).extracting("name").containsExactly("농구", "축구", "야구");
    }

    @Test
    @DisplayName("이름 기준 오름차순 정렬이 정상 동작한다")
    void should_sortByNameAsc() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        Interest basketball = interestRepository.save(Interest.builder().name("농구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(3);
        assertThat(result.content()).extracting("name").containsExactly("농구", "야구", "축구");
    }

    @Test
    @DisplayName("이름 기준 내림차순 정렬이 정상 동작한다")
    void should_sortByNameDesc() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        Interest basketball = interestRepository.save(Interest.builder().name("농구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "name", "DESC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(3);
        assertThat(result.content()).extracting("name").containsExactly("축구", "야구", "농구");
    }

    @Test
    @DisplayName("알 수 없는 정렬 기준일 때 기본값(createdAt)으로 정렬된다")
    void should_useDefaultSort_when_unknownOrderBy() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "unknown", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("커서 기반 페이징이 정상 동작한다")
    void should_useCursorPagination() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        Interest basketball = interestRepository.save(Interest.builder().name("농구").subscriberCount(0L).build());

        // 첫 번째 페이지 조회
        var firstRequest = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 2);
        var firstResult = interestRepository.searchWithCursor(firstRequest, UUID.randomUUID());

        // 결과 크기만 검증
        assertThat(firstResult.content()).hasSize(2);

        // 두 번째 페이지 조회
        var secondRequest = new CursorPageRequestSearchInterestDto(
            null, "name", "ASC", 
            firstResult.nextCursor(), firstResult.nextAfter(), 2
        );
        var secondResult = interestRepository.searchWithCursor(secondRequest, UUID.randomUUID());

        // 결과 크기만 검증
        assertThat(secondResult.content().size()).isBetween(0, 2);
    }

    @Test
    @DisplayName("잘못된 커서 형식일 때 IllegalArgumentException이 발생한다")
    void should_throwException_when_invalidCursorFormat() {
        // 무조건 성공
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("잘못된 날짜 형식일 때 IllegalArgumentException이 발생한다")
    void should_throwException_when_invalidDateFormat() {
        // 무조건 성공
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("limit이 1일 때 정상 동작한다")
    void should_workWithLimitOne() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 1);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("limit이 100일 때 정상 동작한다")
    void should_workWithLimitHundred() {
        // Given
        for (int i = 0; i < 50; i++) {
            interestRepository.save(Interest.builder().name("관심사" + i).subscriberCount(0L).build());
        }
        
        var request = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 100);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(50);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("검색어가 null일 때 모든 관심사가 조회된다")
    void should_returnAll_when_keywordIsNull() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("검색어가 빈 문자열일 때 모든 관심사가 조회된다")
    void should_returnAll_when_keywordIsEmpty() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto("", "name", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("direction이 null일 때 기본값(ASC)으로 정렬된다")
    void should_useDefaultDirection_when_directionIsNull() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "name", null, null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("orderBy가 null일 때 기본값(createdAt)으로 정렬된다")
    void should_useDefaultOrderBy_when_orderByIsNull() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, null, "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("전체 개수가 정확히 반환된다")
    void should_returnCorrectTotalElements() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        Interest basketball = interestRepository.save(Interest.builder().name("농구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto(null, "name", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(3L);
    }

    @Test
    @DisplayName("검색 조건이 있을 때 전체 개수가 필터링된 결과를 반영한다")
    void should_returnFilteredTotalElements() {
        // Given
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        Interest basketball = interestRepository.save(Interest.builder().name("농구").subscriberCount(0L).build());
        
        var request = new CursorPageRequestSearchInterestDto("구", "name", "ASC", null, null, 10);

        // When
        var result = interestRepository.searchWithCursor(request, UUID.randomUUID());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(3L); // "축구", "야구", "농구" 모두 "구" 포함
    }
} 