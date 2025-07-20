package org.project.monewping.domain.notification.exception;

/**
 * 지원하지 않는 리소스 타입인 경우 발생하는 예외입니다.
 *
 * <p>
 *     알림 등록 시, 지원하지 않는 리소스 타입인 경우
 *     GlobalExceptionHandler로 400 Bad Request 응답이 반환됩니다.
 * </p>
 */
public class UnsupportedResourceTypeException extends RuntimeException {

    public UnsupportedResourceTypeException(String resourceType) {
        super("지원하지 않는 리소스 타입입니다: " + resourceType);
    }
}