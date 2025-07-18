package org.project.monewping.domain.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.article.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticlesRepository extends JpaRepository<Articles, UUID>, ArticlesRepositoryCustom {

    boolean existsByOriginalLink(String originalLink);

    List<Articles> findAllByOriginalLinkIn(List<String> originalLinks);

    @Query("SELECT DISTINCT a.source FROM Articles a WHERE a.deleted = false")
    List<String> findDistinctSources();

    // 백업용 날짜 기준 기사 목록 조회
    List<Articles> findAllByPublishedAtBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end);

}
