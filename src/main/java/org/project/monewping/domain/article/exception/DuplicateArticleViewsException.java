package org.project.monewping.domain.article.exception;

public class DuplicateArticleViewsException extends RuntimeException {

    private static final String MESSAGE = "이미 조회한 기사입니다.";

    public DuplicateArticleViewsException() {
        super(MESSAGE);
    }

}
