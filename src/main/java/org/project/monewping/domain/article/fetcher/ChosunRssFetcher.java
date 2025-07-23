package org.project.monewping.domain.article.fetcher;

import java.net.http.HttpClient;
import org.springframework.stereotype.Component;

/**
 * 조선 일보 RSS 수집기
 * AbstractRssFetcher를 상속받아 RSS URL과 소스 이름만 정의
 */
@Component
public class ChosunRssFetcher extends RssArticleFetcher {

    /**
     * 기본 생성자.
     * <p>기본 {@link HttpClient}를 사용하는 {@link RssArticleFetcher}의 생성자를 호출합니다.</p>
     */
    public ChosunRssFetcher() {
        super();
    }

    /**
     * 테스트 또는 커스터마이징된 {@link HttpClient}를 사용하기 위한 생성자.
     *
     * @param client 사용할 HTTP 클라이언트 인스턴스
     */
    public ChosunRssFetcher(HttpClient client) {
        super(client);
    }

    /**
     * 조선일보의 RSS 피드 URL을 반환합니다.
     *
     * @return 조선일보 RSS URL
     */
    @Override
    protected String rssFeedUrl() {
        return "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml";
    }

    /**
     * 기사 출처 이름을 반환합니다.
     *
     * @return "Chosun" 문자열
     */
    @Override
    protected String sourceName() {
        return "Chosun";
    }

}
