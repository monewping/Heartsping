package org.project.monewping.domain.interest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 관심사 키워드 수정 요청 DTO입니다.
 *
 * @param keywords 수정할 키워드 목록
 */
public record InterestUpdateRequest(
    @NotNull(message = "키워드는 필수입니다.")
    @Size(min = 1, max = 10, message = "키워드는 1개 이상 10개 이하로 입력해야 합니다.")
    List<String> keywords
) {} 