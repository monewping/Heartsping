package org.project.monewping.domain.article.service;

import java.time.LocalDate;
import java.util.List;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;

public interface ArticleRestoreService {

    List<ArticleRestoreResultDto> restoreArticlesByRange(LocalDate from, LocalDate to);

}
