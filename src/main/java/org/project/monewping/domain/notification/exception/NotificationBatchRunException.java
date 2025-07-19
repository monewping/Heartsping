package org.project.monewping.domain.notification.exception;

/**
 * 알림 배치 작업 실행 중 예외가 발생했을 때 사용되는 런타임 예외 클래스입니다.
 */
public class NotificationBatchRunException extends RuntimeException {

    public NotificationBatchRunException(String message, Throwable cause) {
        super(message, cause);
    }
}