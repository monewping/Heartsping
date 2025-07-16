package org.project.monewping.domain.article.exception;

import java.util.UUID;

public class ArticleNotFoundException extends RuntimeException {

    /**
     * Constructs an exception indicating that a news article with the specified ID was not found.
     *
     * @param articleId the UUID of the missing article
     */
    public ArticleNotFoundException(UUID articleId) {
        super("해당 뉴스 기사를 찾을 수 없습니다. [ articleId : " + articleId + " ]");
    }
}
