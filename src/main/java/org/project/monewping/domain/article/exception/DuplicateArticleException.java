package org.project.monewping.domain.article.exception;


public class DuplicateArticleException extends RuntimeException {

    public DuplicateArticleException(String originalLink) {
        super("이미 존재하는 뉴스 기사입니다. [ originalLink : " + originalLink + " ]");
    }

}
