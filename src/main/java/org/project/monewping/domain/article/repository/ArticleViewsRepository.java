package org.project.monewping.domain.article.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.monewping.domain.article.entity.ArticleViews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleViewsRepository extends JpaRepository<ArticleViews, UUID> {

    Optional<ArticleViews> findByViewedByAndArticleId(UUID userId, UUID articleId);

    boolean existsByViewedByAndArticleId(UUID viewedBy, UUID articleId);

    // 사용자가 본 기사들(articleId 리스트)에 해당하는 조회 기록 전체를 조회하는 메서드
    List<ArticleViews> findAllByViewedByAndArticleIdIn(UUID viewedBy, List<UUID> articleIds);
}
