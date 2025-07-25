package org.project.monewping.domain.useractivity.exception;

import java.util.UUID;

/**
 * 사용자 활동 내역을 찾을 수 없을 때 발생하는 예외
 */
public class UserActivityNotFoundException extends RuntimeException {

    public UserActivityNotFoundException(UUID userId) {
        super("사용자 활동 내역을 찾을 수 없습니다. userId: " + userId);
    }

    public UserActivityNotFoundException(String message) {
        super(message);
    }

    public UserActivityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}