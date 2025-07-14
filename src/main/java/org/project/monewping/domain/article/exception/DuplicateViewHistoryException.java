package org.project.monewping.domain.article.exception;

public class DuplicateViewHistoryException extends RuntimeException {

    public DuplicateViewHistoryException() {
        super("이미 조회한 기사입니다");
    }

}
