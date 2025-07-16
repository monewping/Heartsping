package org.project.monewping.domain.article.service;

import java.util.List;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;

public interface ArticlesService {

    /**
 * Saves a single news article using the provided request data.
 *
 * @param request the article data to be saved
 */
    void save(ArticleSaveRequest request);

    /**
 * Saves multiple news articles, excluding those with duplicate links.
 *
 * @param requests the list of article save requests to process
 */
    void saveAll(List<ArticleSaveRequest> requests);

}
