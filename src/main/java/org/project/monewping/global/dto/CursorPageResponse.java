package org.project.monewping.global.dto;

import java.util.List;

/**
 * 커서 기반 페이지네이션 응답을 위한 제네릭 DTO
 *
 * @param <T>           콘텐츠 타입
 * @param content       현재 페이지의 데이터 목록
 * @param nextIdAfter   다음 페이지의 시작 ID (옵셔널)
 * @param nextCursor    다음 페이지를 위한 커서 토큰
 * @param size          현재 페이지 크기
 * @param totalElements 전체 요소 수
 * @param hasNext       다음 페이지 존재 여부
 */
public record CursorPageResponse<T>(
        List<T> content,
        Long nextIdAfter,
        String nextCursor,
        int size,
        long totalElements,
        boolean hasNext) {

}
