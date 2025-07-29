package org.project.monewping.domain.interest.exception;

/**
 * 잘못된 요청으로 발생하는 예외입니다.
 *
 * <p>키워드가 null이거나 빈 리스트인 경우 등
 * 유효하지 않은 요청 데이터로 인해 발생하며
 * HTTP 400 상태와 함께 반환됩니다.</p>
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
} 