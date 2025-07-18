package org.project.monewping.domain.article.service;

import java.time.LocalDate;

public interface ArticleBackupService {

    void backupArticlesByDate(LocalDate date);

}
