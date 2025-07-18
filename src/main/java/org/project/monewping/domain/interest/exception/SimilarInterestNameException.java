package org.project.monewping.domain.interest.exception;

/**
 * 80% 이상 유사한 이름의 관심사가 존재할 때 발생하는 예외입니다.
 *
 * <p>Jaro-Winkler 유사도 기반으로 유사한 이름을 감지하여
 * HTTP 409 상태와 함께 반환됩니다.</p>
 */
public class SimilarInterestNameException extends RuntimeException {

    /**
     * 유사한 관심사 이름으로 예외를 생성합니다.
     *
     * <p>관심사 이름과 유사한 기존 관심사들을 포함한 메시지를 생성합니다.</p>
     * @param name 요청된 관심사 이름
     * @param similarNames 유사한 기존 관심사 이름들
     */
    public SimilarInterestNameException(String name, String similarNames) {
        super(String.format("80%% 이상 유사한 이름의 관심사가 존재합니다: %s (유사한 관심사: %s)", name, similarNames));
    }

    /**
     * 유사한 관심사 이름과 원인 예외를 포함하여 예외를 생성합니다.
     *
     * <p>상세 메시지와 원인 예외를 함께 전달합니다.</p>
     * @param name 요청된 관심사 이름
     * @param similarNames 유사한 기존 관심사 이름들
     * @param cause 원인 예외
     */
    public SimilarInterestNameException(String name, String similarNames, Throwable cause) {
        super(String.format("80%% 이상 유사한 이름의 관심사가 존재합니다: %s (유사한 관심사: %s)", name, similarNames), cause);
    }
} 