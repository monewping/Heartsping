package org.project.monewping.domain.article.repository;

import java.util.UUID;
import org.project.monewping.domain.article.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticlesRepository extends JpaRepository<Articles, UUID> {

    boolean existsByOriginalLink(String originalLink);

}
