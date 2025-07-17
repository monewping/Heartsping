package org.project.monewping.domain.article.repository;

import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.article.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticlesRepository extends JpaRepository<Articles, UUID>, ArticlesRepositoryCustom {

    boolean existsByOriginalLink(String originalLink);

    List<Articles> findAllByOriginalLinkIn(List<String> originalLinks);

}
