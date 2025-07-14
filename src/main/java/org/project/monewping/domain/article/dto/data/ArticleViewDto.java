package org.project.monewping.domain.article.dto.data;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(
    UUID id,
    UUID viewedBy,
    UUID articleId,
    LocalDateTime articlePublishedDate
) {

}
