package org.project.monewping.domain.notification.dto;

import java.time.Instant;
import java.util.List;

/**
 * 알림 조회용 커서 기반 페이지네이션 응답 Dto
 *
 * @param content       알림 목록
 * @param nextCursor    다음 페이지 조회를 위한 커서 토큰
 * @param nextAfter     다음 페이지 시작 시점 (createdAt 기준)
 * @param size          현재 페이지 크기
 * @param totalElements 전체 요소 수
 * @param hasNext       다음 페이지 존재 여부
 */
public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) { }