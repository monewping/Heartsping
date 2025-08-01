package org.project.monewping.domain.article.service;

import java.time.LocalDateTime;
import java.util.List;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;

public interface ArticleRestoreService {

    List<ArticleRestoreResultDto> restoreArticlesByRange(LocalDateTime from, LocalDateTime to);

}
