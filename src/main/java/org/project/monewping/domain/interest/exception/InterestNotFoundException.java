package org.project.monewping.domain.interest.exception;

import java.util.UUID;

/**
 * 관심사를 찾을 수 없을 때 발생하는 예외입니다.
 *
 * <p>존재하지 않는 관심사 ID로 조회/수정/삭제 시도 시
 * HTTP 404 상태와 함께 반환됩니다.</p>
 */
public class InterestNotFoundException extends RuntimeException {

    /**
     * 관심사 ID로 예외를 생성합니다.
     *
     * <p>관심사 ID를 포함한 메시지를 생성합니다.</p>
     * @param interestId 찾을 수 없는 관심사 ID
     */
    public InterestNotFoundException(UUID interestId) {
        super(String.format("관심사를 찾을 수 없습니다: %s", interestId));
    }

    /**
     * 관심사 ID와 원인 예외를 포함하여 예외를 생성합니다.
     *
     * <p>상세 메시지와 원인 예외를 함께 전달합니다.</p>
     * @param interestId 찾을 수 없는 관심사 ID
     * @param cause 원인 예외
     */
    public InterestNotFoundException(UUID interestId, Throwable cause) {
        super(String.format("관심사를 찾을 수 없습니다: %s", interestId), cause);
    }
} 