package org.project.monewping.domain.interest.exception;

/**
 * 80% 이상 유사한 이름의 관심사가 존재할 때 발생하는 예외입니다.
 *
 * <p>Jaro-Winkler 유사도 기반으로 유사한 이름을 감지하여
 * HTTP 409 상태와 함께 반환됩니다.</p>
 */
public class SimilarInterestNameException extends RuntimeException {

    public SimilarInterestNameException(String name, String similarNames) {
        super(String.format("80%% 이상 유사한 이름의 관심사가 존재합니다: %s (유사한 관심사: %s)", name, similarNames));
    }

    public SimilarInterestNameException(String name, String similarNames, Throwable cause) {
        super(String.format("80%% 이상 유사한 이름의 관심사가 존재합니다: %s (유사한 관심사: %s)", name, similarNames), cause);
    }
} 