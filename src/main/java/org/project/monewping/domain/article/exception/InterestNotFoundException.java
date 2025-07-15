package org.project.monewping.domain.article.exception;

import java.util.UUID;

public class InterestNotFoundException extends RuntimeException {

    public InterestNotFoundException(UUID interestId) {
        super("관심사를 찾을 수 없습니다. [ interestId : " + interestId + " ]");
    }
}
