package org.project.monewping.domain.article.storage;

import java.time.LocalDate;
import java.util.List;
import org.project.monewping.domain.article.dto.data.ArticleDto;

public interface ArticleBackupStorage {

    List<ArticleDto> load(LocalDate date);

    void save(LocalDate date, List<ArticleDto> articles);

}