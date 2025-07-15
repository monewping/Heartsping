package org.project.monewping.domain.article.service;

import java.util.UUID;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;

public interface ArticleViewsService {

    ArticleViewDto registerView(UUID viewedBy, UUID articleId);

}
