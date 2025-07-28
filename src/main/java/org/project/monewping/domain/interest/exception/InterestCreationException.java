package org.project.monewping.domain.interest.exception;

/**
 * 관심사 생성 중 발생하는 예외입니다.
 *
 * <p>DB 오류 등 예기치 못한 상황에서
 * HTTP 500 상태와 함께 반환됩니다.</p>
 */
public class InterestCreationException extends RuntimeException {

    public InterestCreationException(String message) {
        super(message);
    }

    public InterestCreationException(String message, Throwable cause) {
        super(message, cause);
    }
} 