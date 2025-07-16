package org.project.monewping.domain.interest.dto.response;

import org.project.monewping.domain.interest.dto.InterestDto;

import java.util.List;

/**
 * 관심사 커서 페이지네이션 응답 DTO입니다.
 *
 * @param content 페이지 내용
 * @param nextCursor 다음 페이지 커서
 * @param nextAfter 다음 보조 커서(마지막 요소의 생성 시간)
 * @param size 페이지 크기
 * @param totalElements 총 요소 수
 * @param hasNext 다음 페이지 존재 여부
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
