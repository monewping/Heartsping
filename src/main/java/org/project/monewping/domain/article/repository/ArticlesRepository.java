package org.project.monewping.domain.article.repository;

import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.article.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticlesRepository extends JpaRepository<Articles, UUID> {

    /**
 * Checks whether an article exists with the specified original link.
 *
 * @param originalLink the original link to search for
 * @return true if an article with the given original link exists, false otherwise
 */
boolean existsByOriginalLink(String originalLink);

    /**
 * Retrieves all articles whose original links are included in the specified list.
 *
 * @param originalLinks a list of original link values to match against
 * @return a list of Articles entities with original links matching any value in the provided list
 */
List<Articles> findAllByOriginalLinkIn(List<String> originalLinks);

}
