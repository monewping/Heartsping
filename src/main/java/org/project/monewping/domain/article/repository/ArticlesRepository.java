package org.project.monewping.domain.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.project.monewping.domain.article.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticlesRepository extends JpaRepository<Articles, UUID>, ArticlesRepositoryCustom {

    List<Articles> findAllByOriginalLinkIn(List<String> originalLinks);

    @Query("SELECT DISTINCT a.source FROM Articles a WHERE a.deleted = false")
    List<String> findDistinctSources();

    @Query("SELECT a.originalLink FROM Articles a WHERE a.originalLink IN :originalLinks")
    List<String> findExistingOriginalLinks(List<String> originalLinks);

    List<Articles> findByPublishedAtBetweenAndDeletedFalse(LocalDateTime from, LocalDateTime to);

    // 논리 삭제 메서드
    Optional<Articles> findByIdAndDeletedFalse(UUID id);

}