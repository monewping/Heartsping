package org.project.monewping.domain.article.repository;

import java.util.Optional;
import java.util.UUID;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleViewsRepository extends JpaRepository<ArticleViews, UUID> {

    Optional<ArticleViews> findByViewedByAndArticleId(UUID userId, UUID articleId);

}
