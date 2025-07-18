package org.project.monewping.domain.interest.exception;

/**
 * 중복된 관심사 이름으로 발생하는 예외입니다.
 *
 * <p>이미 존재하는 이름으로 관심사 생성 시
 * HTTP 409 상태와 함께 반환됩니다.</p>
 */
public class DuplicateInterestNameException extends RuntimeException {

    /**
     * 중복된 관심사 이름으로 예외를 생성합니다.
     *
     * <p>관심사 이름을 포함한 메시지를 생성합니다.</p>
     * @param name 중복된 관심사 이름
     */
    public DuplicateInterestNameException(String name) {
        super(String.format("이미 존재하는 관심사 이름입니다: %s", name));
    }

    /**
     * 중복된 관심사 이름과 원인 예외를 포함하여 예외를 생성합니다.
     *
     * <p>상세 메시지와 원인 예외를 함께 전달합니다.</p>
     * @param name 중복된 관심사 이름
     * @param cause 원인 예외
     */
    public DuplicateInterestNameException(String name, Throwable cause) {
        super(String.format("이미 존재하는 관심사 이름입니다: %s", name), cause);
    }
} 