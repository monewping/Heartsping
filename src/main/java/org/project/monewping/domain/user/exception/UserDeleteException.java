package org.project.monewping.domain.user.exception;

import java.util.UUID;

/**
 * 사용자 삭제 권한이 없을 때 발생하는 예외입니다.
 *
 * <p>
 * 본인이 아닌 다른 사용자를 삭제하려고 할 때 발생합니다.
 * 이 예외는 GlobalExceptionHandler에서 HTTP 403 Forbidden 상태로 처리됩니다.
 * </p>
 */
public class UserDeleteException extends RuntimeException {

    public UserDeleteException(UUID userId) {
        super("사용자 삭제 권한이 없습니다. userId: " + userId);
    }

    public UserDeleteException(String message) {
        super(message);
    }

    public UserDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
} 