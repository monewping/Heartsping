package org.project.monewping.domain.article.repository;

import java.util.Optional;
import java.util.UUID;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleViewsRepository extends JpaRepository<ArticleViews, UUID> {

    /**
 * Retrieves an {@code ArticleViews} entity for the specified user and article combination.
 *
 * @param userId    the UUID of the user who viewed the article
 * @param articleId the UUID of the article that was viewed
 * @return an {@code Optional} containing the matching {@code ArticleViews} entity if found, or empty if not found
 */
Optional<ArticleViews> findByViewedByAndArticleId(UUID userId, UUID articleId);

}
