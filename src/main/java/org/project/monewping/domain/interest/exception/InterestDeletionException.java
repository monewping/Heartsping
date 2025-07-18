package org.project.monewping.domain.interest.exception;

/**
 * 관심사 삭제 중 발생하는 예외입니다.
 */
public class InterestDeletionException extends RuntimeException {
    
    public InterestDeletionException(String message) {
        super(message);
    }
    
    public InterestDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
} 