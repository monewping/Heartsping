package org.project.monewping.domain.article.exception;

import java.util.UUID;

public class InterestNotFoundException extends RuntimeException {

    /**
     * Constructs an InterestNotFoundException with a message indicating that the specified interest ID was not found.
     *
     * @param interestId the UUID of the interest that could not be found
     */
    public InterestNotFoundException(UUID interestId) {
        super("관심사를 찾을 수 없습니다. [ interestId : " + interestId + " ]");
    }
}
