package org.project.monewping.domain.interest.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Subscription;
import org.project.monewping.domain.interest.mapper.InterestMapperImpl;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        InterestMapperImpl.class})
@TestPropertySource(properties = "auditing.enabled=true")
class SubscriptionRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    InterestRepository interestRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("구독이 제대로 생성되어 저장된다")
    void subscription_is_saved() {
        // Given
        User user = userRepository.save(User.builder()
                .email("test@email.com")
                .nickname("tester")
                .password("pw")
                .isDeleted(false)
                .build());
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());

        // When
        Subscription subscription = subscriptionRepository.save(new Subscription(user, soccer));

        // Then
        assertThat(subscription.getId()).isNotNull();
        assertThat(subscription.getUser().getId()).isEqualTo(user.getId());
        assertThat(subscription.getInterest().getId()).isEqualTo(soccer.getId());
    }

    @Test
    @DisplayName("userId로 구독한 interestId 목록을 조회할 수 있다")
    void findInterestIdsByUserId_works() {
        // Given
        User user = userRepository.save(User.builder()
                .email("test2@email.com")
                .nickname("tester2")
                .password("pw")
                .isDeleted(false)
                .build());
        Interest soccer = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Interest baseball = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());

        subscriptionRepository.save(new Subscription(user, soccer));
        subscriptionRepository.save(new Subscription(user, baseball));

        // When
        List<java.util.UUID> ids = subscriptionRepository.findInterestIdsByUserId(user.getId());

        // Then
        assertThat(ids).containsExactlyInAnyOrder(soccer.getId(), baseball.getId());
    }

    @Test
    @DisplayName("사용자와 관심사로 구독을 찾을 수 있다")
    void findByUserIdAndInterestId_success() {
        // Given
        User user = userRepository.save(User.builder().email("a@a.com").nickname("a").password("pw").isDeleted(false).build());
        Interest interest = interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        Subscription subscription = subscriptionRepository.save(new Subscription(user, interest));

        // When
        Optional<Subscription> found = subscriptionRepository.findByUserIdAndInterestId(user.getId(), interest.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getInterest().getId()).isEqualTo(interest.getId());
    }

    @Test
    @DisplayName("존재하지 않는 구독은 Optional.empty를 반환한다")
    void findByUserIdAndInterestId_notFound() {
        // Given
        UUID fakeUserId = UUID.randomUUID();
        UUID fakeInterestId = UUID.randomUUID();

        // When
        Optional<Subscription> found = subscriptionRepository.findByUserIdAndInterestId(fakeUserId, fakeInterestId);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("구독 존재 여부를 정확히 반환한다")
    void existsByUserIdAndInterestId() {
        // Given
        User user = userRepository.save(User.builder().email("b@b.com").nickname("b").password("pw").isDeleted(false).build());
        Interest interest = interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        subscriptionRepository.save(new Subscription(user, interest));

        // When & Then
        assertThat(subscriptionRepository.existsByUserIdAndInterestId(user.getId(), interest.getId())).isTrue();
        assertThat(subscriptionRepository.existsByUserIdAndInterestId(UUID.randomUUID(), interest.getId())).isFalse();
        assertThat(subscriptionRepository.existsByUserIdAndInterestId(user.getId(), UUID.randomUUID())).isFalse();
    }
}
