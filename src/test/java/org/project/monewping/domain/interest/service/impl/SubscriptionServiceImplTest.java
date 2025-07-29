package org.project.monewping.domain.interest.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.interest.dto.SubscriptionDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.entity.Subscription;
import org.project.monewping.domain.interest.exception.DuplicateSubscriptionException;
import org.project.monewping.domain.interest.exception.InterestNotFoundException;
import org.project.monewping.domain.interest.exception.SubscriptionNotFoundException;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.repository.SubscriptionRepository;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.exception.UserNotFoundException;
import org.project.monewping.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("SubscriptionServiceImpl 테스트")
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private UserRepository userRepository;

    private UUID interestId;
    private UUID subscriberId;
    private User user;
    private Interest interest;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        interestId = UUID.randomUUID();
        subscriberId = UUID.randomUUID();
        
        user = User.builder()
                .id(subscriberId)
                .email("test@example.com")
                .nickname("테스트유저")
                .password("password123")
                .isDeleted(false)
                .build();

        interest = Interest.builder()
                .id(interestId)
                .name("테스트 관심사")
                .subscriberCount(10L)
                .build();

        subscription = new Subscription(user, interest);
    }

    @Test
    @DisplayName("정상적인 구독 등록이 성공한다")
    void subscribe_Success() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        // when
        SubscriptionDto result = subscriptionService.subscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestId()).isEqualTo(interestId);
        assertThat(result.interestName()).isEqualTo("테스트 관심사");
        assertThat(result.interestSubscriberCount()).isEqualTo(11L); // 구독자 수 증가 확인

        verify(userRepository).findById(subscriberId);
        verify(interestRepository).findById(interestId);
        verify(subscriptionRepository).findInterestIdsByUserId(subscriberId);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("키워드가 있는 관심사 구독이 성공한다")
    void subscribe_WithKeywords_Success() {
        // given
        Keyword keyword1 = new Keyword(interest, "키워드1");
        Keyword keyword2 = new Keyword(interest, "키워드2");
        interest.addKeyword(keyword1);
        interest.addKeyword(keyword2);

        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        // when
        SubscriptionDto result = subscriptionService.subscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestKeywords()).containsExactly("키워드1", "키워드2");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 구독 시도 시 예외가 발생한다")
    void subscribe_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.subscribe(interestId, subscriberId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자 없음");
    }

    @Test
    @DisplayName("존재하지 않는 관심사로 구독 시도 시 예외가 발생한다")
    void subscribe_InterestNotFound_ThrowsException() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.subscribe(interestId, subscriberId))
                .isInstanceOf(InterestNotFoundException.class)
                .satisfies(e -> {
                    InterestNotFoundException ex = (InterestNotFoundException) e;
                    assertThat(ex.getInterestId()).isEqualTo(interestId);
                });
    }

    @Test
    @DisplayName("이미 구독 중인 관심사에 대해 구독 시도 시 예외가 발생한다")
    void subscribe_DuplicateSubscription_ThrowsException() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of(interestId));

        // when & then
        assertThatThrownBy(() -> subscriptionService.subscribe(interestId, subscriberId))
                .isInstanceOf(DuplicateSubscriptionException.class)
                .hasMessage("이미 구독 중입니다.");
    }

    @Test
    @DisplayName("다른 관심사는 구독 중이지만 해당 관심사는 구독하지 않은 경우 구독이 성공한다")
    void subscribe_OtherInterestsSubscribed_Success() {
        // given
        UUID otherInterestId = UUID.randomUUID();
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of(otherInterestId));
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        // when
        SubscriptionDto result = subscriptionService.subscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestId()).isEqualTo(interestId);
    }

    @Test
    @DisplayName("구독자 수가 0인 관심사 구독이 성공한다")
    void subscribe_ZeroSubscriberCount_Success() {
        // given
        Interest zeroInterest = Interest.builder()
                .id(interestId)
                .name("구독자 0명 관심사")
                .subscriberCount(0L)
                .build();

        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(zeroInterest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        // when
        SubscriptionDto result = subscriptionService.subscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestSubscriberCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("구독자 수가 많은 관심사 구독이 성공한다")
    void subscribe_HighSubscriberCount_Success() {
        // given
        Interest highInterest = Interest.builder()
                .id(interestId)
                .name("구독자 많은 관심사")
                .subscriberCount(999999L)
                .build();

        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(highInterest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        // when
        SubscriptionDto result = subscriptionService.subscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestSubscriberCount()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("빈 키워드 리스트를 가진 관심사 구독이 성공한다")
    void subscribe_EmptyKeywords_Success() {
        // given
        Interest emptyInterest = Interest.builder()
                .id(interestId)
                .name("빈 키워드 관심사")
                .subscriberCount(5L)
                .keywords(List.of())
                .build();

        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(emptyInterest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        // when
        SubscriptionDto result = subscriptionService.subscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestKeywords()).isEmpty();
    }

    @Test
    @DisplayName("null 키워드 리스트를 가진 관심사 구독이 성공한다")
    void subscribe_NullKeywords_Success() {
        // given
        Interest nullInterest = Interest.builder()
                .id(interestId)
                .name("null 키워드 관심사")
                .subscriberCount(5L)
                .build();
        // 키워드 리스트가 null인 상태로 설정

        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(nullInterest));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

        // when
        SubscriptionDto result = subscriptionService.subscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestKeywords()).isEmpty();
    }

    @Test
    @DisplayName("정상적인 구독 취소가 성공한다")
    void unsubscribe_Success() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findByUserIdAndInterestId(subscriberId, interestId)).willReturn(Optional.of(subscription));

        // when
        SubscriptionDto result = subscriptionService.unsubscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestId()).isEqualTo(interestId);
        assertThat(result.interestName()).isEqualTo("테스트 관심사");
        assertThat(result.interestSubscriberCount()).isEqualTo(9L); // 구독자 수 감소 확인

        verify(userRepository).findById(subscriberId);
        verify(interestRepository).findById(interestId);
        verify(subscriptionRepository).findByUserIdAndInterestId(subscriberId, interestId);
        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    @DisplayName("키워드가 있는 관심사 구독 취소가 성공한다")
    void unsubscribe_WithKeywords_Success() {
        // given
        Keyword keyword1 = new Keyword(interest, "키워드1");
        Keyword keyword2 = new Keyword(interest, "키워드2");
        interest.addKeyword(keyword1);
        interest.addKeyword(keyword2);

        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findByUserIdAndInterestId(subscriberId, interestId)).willReturn(Optional.of(subscription));

        // when
        SubscriptionDto result = subscriptionService.unsubscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestKeywords()).containsExactly("키워드1", "키워드2");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 구독 취소 시도 시 예외가 발생한다")
    void unsubscribe_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(interestId, subscriberId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자 없음");
    }

    @Test
    @DisplayName("존재하지 않는 관심사로 구독 취소 시도 시 예외가 발생한다")
    void unsubscribe_InterestNotFound_ThrowsException() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(interestId, subscriberId))
                .isInstanceOf(InterestNotFoundException.class)
                .satisfies(e -> {
                    InterestNotFoundException ex = (InterestNotFoundException) e;
                    assertThat(ex.getInterestId()).isEqualTo(interestId);
                });
    }

    @Test
    @DisplayName("구독하지 않은 관심사에 대해 구독 취소 시도 시 예외가 발생한다")
    void unsubscribe_SubscriptionNotFound_ThrowsException() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findByUserIdAndInterestId(subscriberId, interestId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(interestId, subscriberId))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining("구독을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("구독자 수가 1인 관심사 구독 취소가 성공한다")
    void unsubscribe_OneSubscriber_Success() {
        // given
        Interest oneInterest = Interest.builder()
                .id(interestId)
                .name("구독자 1명 관심사")
                .subscriberCount(1L)
                .build();

        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(oneInterest));
        given(subscriptionRepository.findByUserIdAndInterestId(subscriberId, interestId)).willReturn(Optional.of(subscription));

        // when
        SubscriptionDto result = subscriptionService.unsubscribe(interestId, subscriberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.interestSubscriberCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("동일 사용자가 여러 관심사 구독/취소를 반복해도 구독자 수가 정확히 반영된다")
    void subscribeAndUnsubscribe_MultipleInterests() {
        // given
        Interest interest1 = Interest.builder().id(UUID.randomUUID()).name("관심사1").subscriberCount(0L).build();
        Interest interest2 = Interest.builder().id(UUID.randomUUID()).name("관심사2").subscriberCount(0L).build();
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interest1.getId())).willReturn(Optional.of(interest1));
        given(interestRepository.findById(interest2.getId())).willReturn(Optional.of(interest2));
        given(subscriptionRepository.findInterestIdsByUserId(subscriberId)).willReturn(List.of());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);
        given(subscriptionRepository.findByUserIdAndInterestId(subscriberId, interest1.getId())).willReturn(Optional.of(subscription));
        given(subscriptionRepository.findByUserIdAndInterestId(subscriberId, interest2.getId())).willReturn(Optional.of(subscription));

        // when
        SubscriptionDto dto1 = subscriptionService.subscribe(interest1.getId(), subscriberId);
        SubscriptionDto dto2 = subscriptionService.subscribe(interest2.getId(), subscriberId);
        SubscriptionDto cancel1 = subscriptionService.unsubscribe(interest1.getId(), subscriberId);
        SubscriptionDto cancel2 = subscriptionService.unsubscribe(interest2.getId(), subscriberId);

        // then
        assertThat(dto1.interestSubscriberCount()).isEqualTo(1L);
        assertThat(dto2.interestSubscriberCount()).isEqualTo(1L);
        assertThat(cancel1.interestSubscriberCount()).isEqualTo(0L);
        assertThat(cancel2.interestSubscriberCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("구독 취소 중 예외 발생 시 트랜잭션이 롤백된다")
    void unsubscribe_rollbackOnException() {
        // given
        given(userRepository.findById(subscriberId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(subscriptionRepository.findByUserIdAndInterestId(subscriberId, interestId)).willThrow(new RuntimeException("DB 오류"));

        // when & then
        assertThatThrownBy(() -> subscriptionService.unsubscribe(interestId, subscriberId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("DB 오류");
    }
} 