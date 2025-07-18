package org.project.monewping.domain.article.service;

import java.time.LocalDate;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;

public interface ArticleRestoreService {

    ArticleRestoreResultDto restoreArticlesByDate(LocalDate date);

}
