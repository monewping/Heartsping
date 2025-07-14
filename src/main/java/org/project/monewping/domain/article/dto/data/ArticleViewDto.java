package org.project.monewping.domain.article.dto.data;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(
    UUID userId,
    UUID articleId,
    LocalDateTime viewedAt
) {

}
