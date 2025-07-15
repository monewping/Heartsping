package org.project.monewping.domain.interest.dto.response;

import org.project.monewping.domain.interest.dto.InterestDto;

import java.util.List;

/**
 * 관심사 커서 페이지네이션 응답 DTO입니다.
 *
 * - content: 관심사 정보(InterestDto) 목록
 * - nextCursor: 다음 페이지 커서 값
 * - nextAfter: 다음 페이지 보조 커서(createdAt 등)
 * - size: 현재 페이지 크기
 * - totalElements: 전체 데이터 개수
 * - hasNext: 다음 페이지 존재 여부
 */
public record CursorPageResponseInterestDto(
        List<InterestDto> content,
        String nextCursor,
        String nextAfter,
        Integer size,
        Long totalElements,
        Boolean hasNext
) {
}
