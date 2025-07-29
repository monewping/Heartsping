package org.project.monewping.global.exception;

/**
 * 로그인 실패 시 발생하는 사용자 정의 예외
 * 
 * <p>
 * 로그인 시 이메일 또는 비밀번호가 일치하지 않는 경우 발생합니다.
 * RuntimeException을 상속하여 언체크 예외로 구현되었습니다.
 * </p>
 * 
 * <p>
 * 이 예외는 GlobalExceptionHandler에서 HTTP 401 Unauthorized 상태로 처리됩니다.
 * </p>
 * 
 */
public class LoginFailedException extends RuntimeException {

    /**
     * 메시지만을 포함하는 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     */
    public LoginFailedException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함하는 예외를 생성합니다.
     * 
     * @param message 예외 메시지
     * @param cause   예외의 원인이 되는 Throwable 객체
     */
    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}