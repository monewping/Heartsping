
package org.project.monewping.domain.interest.service;

import org.project.monewping.domain.interest.dto.SubscriptionDto;
import java.util.UUID;

public interface SubscriptionService {
    SubscriptionDto subscribe(UUID interestId, UUID subscriberId);
    
    /**
     * 관심사 구독을 취소합니다.
     *
     * @param interestId 구독 취소할 관심사 ID
     * @param subscriberId 구독자(사용자) ID
     * @return 구독 취소 정보 DTO
     */
    SubscriptionDto unsubscribe(UUID interestId, UUID subscriberId);
}