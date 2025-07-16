package org.project.monewping.domain.article.service;

import java.util.UUID;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;

public interface ArticleViewsService {

    /**
 * Registers a view for the specified article by the given user.
 *
 * @param viewedBy the UUID of the user viewing the article
 * @param articleId the UUID of the article being viewed
 * @return an ArticleViewDto representing the registered view
 */
ArticleViewDto registerView(UUID viewedBy, UUID articleId);

}
