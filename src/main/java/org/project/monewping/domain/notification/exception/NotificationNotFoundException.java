package org.project.monewping.domain.notification.exception;

import java.util.UUID;

/**
 * 요청한 ID에 해당하는 알림을 찾을 수 없을 때 던져지는 예외입니다.
 *
 * <p>
 *     서비스 계층이나 컨트롤러에서 특정 알림을 조회했으나,
 *     ID 값에 해당하는 알림이 존재하지 않을 경우 이 예외를 발생시킵니다.
 *     이 예외는 GlobalExceptionHandler에서 HTTP 404 Not Found 상태로 처리됩니다.
 * </p>
 */
public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID userId, UUID notificationId) {
        super("존재하지 않는 알림입니다 (userId: " + userId + "notificationId: " + notificationId + ")");
    }
}