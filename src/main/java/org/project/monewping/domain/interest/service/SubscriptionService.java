
package org.project.monewping.domain.interest.service;

import org.project.monewping.domain.interest.dto.SubscriptionDto;
import java.util.UUID;

public interface SubscriptionService {
    SubscriptionDto subscribe(UUID interestId, UUID subscriberId);
}