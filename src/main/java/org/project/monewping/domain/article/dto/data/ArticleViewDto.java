package org.project.monewping.domain.article.dto.data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(

    @NotNull(message = "id는 필수입니다.")
    UUID id,

    @NotNull(message = "조회한 사용자 ID는 필수입니다.")
    UUID viewedBy,

    @NotNull(message = "조회하는 뉴스 기사 ID는 필수입니다")
    UUID articleId,

    @NotNull(message = "조회한 날짜는 필수입니다")
    LocalDateTime articlePublishedDate
) {

}
