package org.project.monewping.domain.interest.exception;

/**
 * 중복된 관심사 이름으로 발생하는 예외입니다.
 *
 * <p>이미 존재하는 이름으로 관심사 생성 시
 * HTTP 409 상태와 함께 반환됩니다.</p>
 */
public class DuplicateInterestNameException extends RuntimeException {

    public DuplicateInterestNameException(String name) {
        super(String.format("이미 존재하는 관심사 이름입니다: %s", name));
    }

    public DuplicateInterestNameException(String name, Throwable cause) {
        super(String.format("이미 존재하는 관심사 이름입니다: %s", name), cause);
    }
} 