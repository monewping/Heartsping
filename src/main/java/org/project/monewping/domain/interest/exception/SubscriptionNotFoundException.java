package org.project.monewping.domain.interest.exception;

import java.util.UUID;

/**
 * 구독을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(UUID interestId, UUID userId) {
        super(String.format("관심사 ID %s에 대한 사용자 ID %s의 구독을 찾을 수 없습니다.", interestId, userId));
    }
} 