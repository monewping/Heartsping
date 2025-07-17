package org.project.monewping.domain.article.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record ArticleSearchRequest(
    String keyword,
    UUID interestId,
    String source,
    LocalDate date,
    String sortBy,
    UUID cursorId,
    int size
) {

}
