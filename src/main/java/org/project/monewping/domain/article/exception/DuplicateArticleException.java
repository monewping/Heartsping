package org.project.monewping.domain.article.exception;


public class DuplicateArticleException extends RuntimeException {

    /**
     * Constructs a DuplicateArticleException indicating that a news article with the specified original link already exists.
     *
     * @param originalLink the original link of the duplicate news article
     */
    public DuplicateArticleException(String originalLink) {
        super("이미 존재하는 뉴스 기사입니다. [ originalLink : " + originalLink + " ]");
    }

}
