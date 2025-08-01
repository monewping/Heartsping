package org.project.monewping.domain.article.exception;

import java.time.LocalDateTime;

/**
 * 뉴스 기사 복원 요청 시 시작일이 종료일보다 늦는 잘못된 날짜 범위를 전달한 경우 발생하는 예외입니다.
 *
 * <p>예를 들어, 사용자가 {@code from=2025-08-01T00:00:00}이고
 * {@code to=2025-07-31T23:59:59}처럼 시작일이 종료일보다 이후인 경우에 이 예외가 발생합니다.</p>
 *
 * <p>이 예외는 {@link org.project.monewping.domain.article.service.ArticleRestoreService} 구현체에서
 * 파라미터 유효성 검사를 수행하는 중에 사용됩니다.</p>
 */
public class InvalidRestoreRangeException extends RuntimeException {

    /**
     * 잘못된 날짜 범위로 복원 요청이 들어온 경우 예외를 생성합니다.
     *
     * @param from 복원 시작일 (예: {@code 2025-08-01T00:00:00})
     * @param to 복원 종료일 (예: {@code 2025-07-31T23:59:59})
     */
    public InvalidRestoreRangeException(LocalDateTime from, LocalDateTime to) {
        super("복구 시작일( " + from + " )은 종료일( " + to + " )보다 빠르거나 같아야 합니다");
    }
}
