package org.project.monewping.domain.article.repository;

import java.util.Optional;
import java.util.UUID;
import org.project.monewping.domain.article.entity.NewsViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsViewHistoryRepository extends JpaRepository<NewsViewHistory, UUID> {

    Optional<NewsViewHistory> findByUserIdAndArticleId(UUID userId, UUID articleId);

}
