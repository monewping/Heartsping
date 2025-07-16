package org.project.monewping.domain.interest.dto.request;

import jakarta.validation.constraints.*;

/**
 * 관심사 커서 기반 페이지네이션 요청 DTO입니다.
 *
 * @param keyword 검색어(관심사 이름 또는 키워드)
 * @param orderBy 정렬 속성(name, subscriberCount)
 * @param direction 정렬 방향(ASC, DESC)
 * @param cursor 커서 값(페이징용)
 * @param after 보조 커서(createdAt 등)
 * @param limit 페이지 크기(1~100)
 */
public record CursorPageRequestSearchInterestDto(
    String keyword,
    @Pattern(regexp = "name|subscriberCount", message = "정렬 속성은 name 또는 subscriberCount만 허용됩니다.")
    String orderBy,
    @Pattern(regexp = "ASC|DESC", message = "정렬 방향은 ASC 또는 DESC만 허용됩니다.")
    String direction,
    String cursor,
    String after,
    @NotNull(message = "페이지 크기는 필수입니다.")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하이어야 합니다.")
    Integer limit
) {}
