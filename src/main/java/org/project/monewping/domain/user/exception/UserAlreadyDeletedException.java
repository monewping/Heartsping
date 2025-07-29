package org.project.monewping.domain.user.exception;

import java.util.UUID;

/**
 * 이미 삭제된 사용자에 대한 작업을 시도할 때 발생하는 예외입니다.
 *
 * <p>
 * 논리적으로 삭제된 사용자에 대해 추가 작업을 시도할 때 발생합니다.
 * 이 예외는 GlobalExceptionHandler에서 HTTP 404 Not Found 상태로 처리됩니다.
 * </p>
 */
public class UserAlreadyDeletedException extends RuntimeException {

    public UserAlreadyDeletedException(UUID userId) {
        super("이미 삭제된 사용자입니다. userId: " + userId);
    }

    public UserAlreadyDeletedException(String message) {
        super(message);
    }

    public UserAlreadyDeletedException(String message, Throwable cause) {
        super(message, cause);
    }
} 