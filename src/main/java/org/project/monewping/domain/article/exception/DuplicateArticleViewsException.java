package org.project.monewping.domain.article.exception;

public class DuplicateArticleViewsException extends RuntimeException {

    private static final String MESSAGE = "이미 조회한 기사입니다.";

    /**
     * Constructs a new DuplicateArticleViewsException with a predefined message indicating the article has already been viewed.
     */
    public DuplicateArticleViewsException() {
        super(MESSAGE);
    }

}
