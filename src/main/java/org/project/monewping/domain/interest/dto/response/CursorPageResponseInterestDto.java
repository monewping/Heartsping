package org.project.monewping.domain.interest.dto.response;

import org.project.monewping.domain.interest.dto.InterestDto;

import java.util.List;

public record CursorPageResponseInterestDto(
        List<InterestDto> content,
        String nextCursor,
        String nextAfter,
        Integer size,
        Long totalElements,
        Boolean hasNext
) {
}
