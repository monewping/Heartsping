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
} 