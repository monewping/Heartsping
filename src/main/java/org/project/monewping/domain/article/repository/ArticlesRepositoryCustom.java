package org.project.monewping.domain.article.repository;

import java.util.List;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.entity.Articles;

public interface ArticlesRepositoryCustom {

    List<Articles> searchArticles(ArticleSearchRequest request);

    long countArticles(ArticleSearchRequest request);

}
