package org.project.monewping.domain.interest.service.impl;

import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.interest.dto.SubscriptionDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Subscription;
import org.project.monewping.domain.interest.exception.SubscriptionNotFoundException;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.repository.SubscriptionRepository;
import org.project.monewping.domain.interest.service.SubscriptionService;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * 구독(Subscription) 관련 서비스 구현체입니다.
 *
 * <p>구독 등록, 중복 구독 방지, 구독자 수 증가 등 구독 도메인 핵심 비즈니스 로직을 처리합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InterestRepository interestRepository;
    private final UserRepository userRepository;

    /**
     * 관심사 구독을 등록합니다.
     *
     * <p>이미 구독 중인 경우 예외를 발생시키며,
     * 구독 성공 시 관심사의 구독자 수를 1 증가시킵니다.</p>
     *
     * @param interestId 구독할 관심사 ID
     * @param subscriberId 구독자(사용자) ID
     * @return 구독 정보 DTO
     * @throws IllegalArgumentException 사용자 또는 관심사가 존재하지 않을 때
     * @throws IllegalStateException 이미 구독 중일 때
     */
    @Override
    @Transactional
    public SubscriptionDto subscribe(UUID interestId, UUID subscriberId) {
        User user = userRepository.findById(subscriberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new IllegalArgumentException("관심사 없음"));

        // 중복 구독 방지
        if (subscriptionRepository.findInterestIdsByUserId(user.getId()).contains(interestId)) {
            throw new IllegalStateException("이미 구독 중입니다.");
        }

        Subscription subscription = new Subscription(user, interest);
        Subscription saved = subscriptionRepository.save(subscription);

        // 구독자 수 증가
        interest.increaseSubscriber();

        return SubscriptionDto.builder()
            .id(saved.getId())
            .interestId(interest.getId())
            .interestName(interest.getName())
            .interestKeywords(interest.getKeywords().stream().map(k -> k.getName()).toList())
            .interestSubscriberCount(interest.getSubscriberCount())
            .createdAt(saved.getCreatedAt())
            .build();
    }

    /**
     * 관심사 구독을 취소합니다.
     *
     * <p>구독이 존재하지 않는 경우 예외를 발생시키며,
     * 구독 취소 성공 시 관심사의 구독자 수를 1 감소시킵니다.</p>
     *
     * @param interestId 구독 취소할 관심사 ID
     * @param subscriberId 구독자(사용자) ID
     * @return 구독 취소 정보 DTO
     * @throws IllegalArgumentException 사용자 또는 관심사가 존재하지 않을 때
     * @throws SubscriptionNotFoundException 구독이 존재하지 않을 때
     */
    @Override
    @Transactional
    public SubscriptionDto unsubscribe(UUID interestId, UUID subscriberId) {
        User user = userRepository.findById(subscriberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new IllegalArgumentException("관심사 없음"));

        // 구독 존재 여부 확인
        Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(user.getId(), interestId)
            .orElseThrow(() -> new SubscriptionNotFoundException(interestId, user.getId()));

        // 구독 삭제
        subscriptionRepository.delete(subscription);

        // 구독자 수 감소
        interest.decreaseSubscriber();

        return SubscriptionDto.builder()
            .id(subscription.getId())
            .interestId(interest.getId())
            .interestName(interest.getName())
            .interestKeywords(interest.getKeywords().stream().map(k -> k.getName()).toList())
            .interestSubscriberCount(interest.getSubscriberCount())
            .createdAt(subscription.getCreatedAt())
            .build();
    }
}