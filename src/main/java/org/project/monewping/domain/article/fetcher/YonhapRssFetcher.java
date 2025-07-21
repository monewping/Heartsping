package org.project.monewping.domain.article.fetcher;

import java.net.http.HttpClient;
import org.springframework.stereotype.Component;

/**
 * 연합 뉴스 RSS 수집기
 * AbstractRssFetcher를 상속받아 RSS URL과 소스 이름만 정의
 */
@Component
public class YonhapRssFetcher extends RssArticleFetcher {

    public YonhapRssFetcher() {
        super();
    }

    public YonhapRssFetcher(HttpClient client) {
        super(client);
    }

    /**
     * 연합뉴스의 RSS 피드 URL을 반환합니다.
     *
     * @return 연합뉴스 RSS 피드 URL
     */
    @Override
    protected String rssFeedUrl() {
        return "http://www.yonhapnewstv.co.kr/browse/feed/";
    }

    /**
     * 기사 출처 이름을 반환합니다.
     *
     * @return "Yonhap"
     */
    @Override
    protected String sourceName() {
        return "Yonhap";
    }
}
