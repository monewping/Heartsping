package org.project.monewping.domain.article.fetcher;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;

/**
 * 공통 RSS 뉴스 수집 추상 클래스.
 * 템플릿 메서드 패턴을 적용하여 공통 로직을 제공하고, 각 언론사는 URL과 source 이름만 제공.
 */
@Slf4j
public abstract class RssArticleFetcher implements ArticleFetcher {

    private final HttpClient client;

    // 테스트 용이성을 위한 HttpClient 주입 생성자
    protected RssArticleFetcher(HttpClient client) {
        this.client = client;
    }

    // 기본 HttpClient 사용 생성자
    protected RssArticleFetcher() {
        this(HttpClient.newHttpClient());
    }

    /**
     * 뉴스 기사 수집 템플릿 메서드.
     * - RSS XML을 요청하고 파싱
     * - 각 item에서 title, link, description, pubDate 추출
     * - 키워드 포함된 기사만 ArticleSaveRequest로 변환
     */
    @Override
    public List<ArticleSaveRequest> fetch(UUID interestId, String keyword) {
        log.info("[{}] RSS 뉴스 수집 시작 - keyword: {}", sourceName(), keyword);

        try {
            // 1. RSS 요청 생성
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(rssFeedUrl()))
                .GET()
                .build();

            // 2. 응답 수신
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("[{}] RSS 응답 실패 - status: {}", sourceName(), response.statusCode());
                return List.of();
            }

            // 3. Jsoup XML 파싱
            org.jsoup.nodes.Document doc = Jsoup.parse(response.body(), "", org.jsoup.parser.Parser.xmlParser());
            Elements items = doc.select("item");

            List<ArticleSaveRequest> articles = new ArrayList<>();

            // 4. 각 item 요소 순회
            for (Element item : items) {
                Element titleElement = item.selectFirst("title");
                String title = titleElement != null ? titleElement.text() : null;

                Element linkElement = item.selectFirst("link");
                String link = linkElement != null ? linkElement.text() : null;

                Element descElement = item.selectFirst("description");
                String description = descElement != null ? descElement.text() : null;

                Element pubDateElement = item.selectFirst("pubDate");
                String pubDate = pubDateElement != null ? pubDateElement.text() : null;

                if (!containsKeyword(title, description, keyword)) continue;

                LocalDateTime publishedAt = parsePubDate(pubDate);

                articles.add(new ArticleSaveRequest(
                    interestId,
                    sourceName(),
                    link,
                    title,
                    description != null ? description : "",
                    publishedAt
                ));
            }


            log.info("[{}] RSS 수집 완료 - 총 {}건", sourceName(), articles.size());
            return articles;

        } catch (Exception e) {
            log.error("[{}] RSS 뉴스 수집 실패", sourceName(), e);
            return List.of();
        }
    }

    /**
     * 주어진 제목 또는 요약 중 하나라도 키워드를 포함하는지 확인합니다.
     * - 키워드가 null 또는 공백인 경우, 필터링 없이 true 반환
     * - 대소문자 구분 없이 검사합니다.
     */
    private boolean containsKeyword(String title, String description, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;

        String lowerKeyword = keyword.toLowerCase();

        boolean titleContains = title != null && title.toLowerCase().contains(lowerKeyword);
        boolean descriptionContains = description != null && description.toLowerCase().contains(lowerKeyword);

        return titleContains || descriptionContains;
    }

    /**
     * pubDate를 LocalDateTime으로 변환
     * 형식 오류 시 현재 시간으로 대체
     */
    private LocalDateTime parsePubDate(String pubDate) {
        if (pubDate == null || pubDate.isEmpty()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (Exception e) {
            log.warn("발행일 파싱 실패, 기본값 사용: {}", pubDate);
            return LocalDateTime.now();
        }
    }

    /**
     * 하위 클래스가 구현해야 할 RSS 피드 URL
     */
    protected abstract String rssFeedUrl();

    /**
     * 하위 클래스가 구현해야 할 언론사 이름
     */
    protected abstract String sourceName();
}
