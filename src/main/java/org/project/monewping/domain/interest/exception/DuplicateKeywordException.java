package org.project.monewping.domain.interest.exception;

/**
 * 중복된 키워드 예외입니다.
 *
 * <p>이미 존재하는 키워드와 동일한 키워드를 추가하려고 할 때 발생합니다.</p>
 */
public class DuplicateKeywordException extends RuntimeException {

    public DuplicateKeywordException(String keyword) {
        super("중복된 키워드입니다: " + keyword);
    }

    public DuplicateKeywordException(String message, Throwable cause) {
        super(message, cause);
    }
} 