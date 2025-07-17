package org.project.monewping.domain.article.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleSearchRequest(
    String keyword,
    UUID interestId,
    List<String> sourceIn,
    LocalDateTime publishDateFrom,
    LocalDateTime publishDateTo,
    String orderBy,
    String direction,
    String cursor,
    LocalDateTime after,
    int limit,
    UUID requestUserId
) {

}
