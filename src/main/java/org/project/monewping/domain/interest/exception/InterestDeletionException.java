package org.project.monewping.domain.interest.exception;

/**
 * 관심사 삭제 중 발생하는 예외입니다.
 *
 * <p>관심사 삭제 과정에서 DB 오류 등 예기치 못한 상황이 발생할 때
 * HTTP 500 상태와 함께 반환됩니다.</p>
 */
public class InterestDeletionException extends RuntimeException {
    
    public InterestDeletionException(String message) {
        super(message);
    }
    
    public InterestDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
} 