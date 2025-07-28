package org.project.monewping.domain.interest.exception;

/**
 * 중복된 구독으로 발생하는 예외입니다.
 *
 * <p>이미 구독 중인 관심사를 다시 구독하려고 할 때 발생하며
 * HTTP 409 상태와 함께 반환됩니다.</p>
 */
public class DuplicateSubscriptionException extends RuntimeException {
    public DuplicateSubscriptionException(String message) {
        super(message);
    }

    public DuplicateSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

}
