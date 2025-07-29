package org.project.monewping.domain.interest.exception;

import java.util.UUID;

/**
 * 관심사를 찾을 수 없는 예외입니다.
 *
 * <p>존재하지 않는 관심사 ID로 조회하거나 수정하려고 할 때 발생합니다.</p>
 */
public class InterestNotFoundException extends RuntimeException {

    private final UUID interestId;

    public InterestNotFoundException(UUID interestId) {
        super("관심사를 찾을 수 없습니다: " + interestId);
        this.interestId = interestId;
    }

    public InterestNotFoundException(String message, Throwable cause, UUID interestId) {
        super(message, cause);
        this.interestId = interestId;
    }

    public UUID getInterestId() {
        return interestId;
    }
}