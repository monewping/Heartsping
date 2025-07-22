package org.project.monewping.domain.article.fetcher;

import java.net.http.HttpClient;
import org.springframework.stereotype.Component;

/**
 * 한국 경제 RSS 수집기
 * AbstractRssFetcher를 상속받아 RSS URL과 소스 이름만 정의
 */
@Component
public class HankyungRssFetcher extends RssArticleFetcher {

    public HankyungRssFetcher() {
        super();
    }

    public HankyungRssFetcher(HttpClient client) {
        super(client);
    }

    /**
     * 한국경제의 RSS 피드 URL을 반환합니다.
     *
     * @return 한국경제 RSS 피드 URL
     */
    @Override
    protected String rssFeedUrl() {
        return "https://www.hankyung.com/feed/all-news";
    }

    /**
     * 기사 출처 이름을 반환합니다.
     *
     * @return "Hankyung"
     */
    @Override
    protected String sourceName() {
        return "Hankyung";
    }
}
