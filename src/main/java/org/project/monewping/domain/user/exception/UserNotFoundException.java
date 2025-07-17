package org.project.monewping.domain.user.exception;

/**
 * 사용자가 존재하지 않을 때 발생하는 사용자 정의 예외
 *
 * <p>
 * UserId 값으로 사용자가 조회되지 않는 경우 발생합니다.
 * RuntimeException을 상속하여 언체크 예외로 구현되었습니다.
 * </p>
 *
 * <p>
 * 이 예외는 GlobalExceptionHandler에서 HTTP 409 Conflict 상태로 처리됩니다.
 * </p>
 *
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * 메시지만을 포함하는 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
