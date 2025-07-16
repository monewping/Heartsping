package org.project.monewping.domain.article.fetcher;

import java.util.List;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;

/**
 * 외부 뉴스 기사 수집기의 공통 인터페이스
 */
public interface ArticleFetcher {

    /**
 * Fetches news articles from external sources based on the provided keyword.
 *
 * @param keyword the search keyword used to collect relevant news articles
 * @return a list of ArticleSaveRequest objects representing the fetched articles
 */
    List<ArticleSaveRequest> fetch(String keyword);

}
