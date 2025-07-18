package org.project.monewping.domain.interest.exception;

/**
 * 관심사 생성 중 발생하는 예외입니다.
 *
 * <p>DB 오류 등 예기치 못한 상황에서
 * HTTP 500 상태와 함께 반환됩니다.</p>
 */
public class InterestCreationException extends RuntimeException {

    /**
     * 지정된 메시지로 예외를 생성합니다.
     *
     * <p>사용자 정의 메시지를 포함합니다.</p>
     * @param message 예외 메시지
     */
    public InterestCreationException(String message) {
        super(message);
    }

    /**
     * 지정된 메시지와 원인 예외를 포함하여 예외를 생성합니다.
     *
     * <p>상세 메시지와 원인 예외를 함께 전달합니다.</p>
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public InterestCreationException(String message, Throwable cause) {
        super(message, cause);
    }
} 