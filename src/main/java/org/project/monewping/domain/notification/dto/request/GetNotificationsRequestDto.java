package org.project.monewping.domain.notification.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 알림 조회를 위한 쿼리 파라미터를 담은 DTO입니다.
 *
 * @param cursor  페이지 시작을 나타내는 커서 토큰 ({@code null} 허용)
 * @param after   이 시간 이후의 알림을 조회할 기준 시각 (ISO-8601 포맷, {@code null} 허용)
 * @param limit   조회할 알림 개수 (필수, 1 이상이어야 함)
 */
public record GetNotificationsRequestDto(
    String cursor,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
    @NotNull @Min(1) int limit
) { }