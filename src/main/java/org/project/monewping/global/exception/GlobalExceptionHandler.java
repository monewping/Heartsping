package org.project.monewping.global.exception;

import org.project.monewping.domain.notification.exception.NotificationNotFoundException;
import org.project.monewping.domain.notification.exception.UnsupportedResourceTypeException;
import org.project.monewping.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 지원하지 않는 리소스 타입 예외를 처리합니다.
     *
     * <p>UnsupportedResourceTypeException이 발생하면 이 메서드가 호출되어
     * HTTP 400 Bad Request 상태 코드와 함께 에러 정보를 담은 ErrorResponse를 반환합니다.</p>
     *
     * @param ex 발생한 UnsupportedResourceTypeException
     * @return HTTP 400 상태와 에러 메시지를 담은 ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(UnsupportedResourceTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedResourceTypeException(
        UnsupportedResourceTypeException ex
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 알림을 찾을 수 없을 때 발생하는 예외를 처리합니다.
     *
     * <p>NotificationNotFoundException이 발생하면 이 메서드가 호출되어
     * HTTP 404 Not Found 상태 코드와 함께 에러 정보를 담은 ErrorResponse를 반환합니다.</p>
     *
     * @param ex 발생한 NotificationNotFoundException
     * @return HTTP 404 상태와 에러 메시지를 담은 ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFoundException(
        NotificationNotFoundException ex
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
