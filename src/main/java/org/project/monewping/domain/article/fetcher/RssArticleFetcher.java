package org.project.monewping.domain.article.fetcher;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
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
    public List<ArticleSaveRequest> fetch(UUID interestId, List<String> keywords) {
        log.info("[{}] RSS 뉴스 수집 시작 - keyword: {}", sourceName(), keywords);

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
            Document doc = Jsoup.parse(response.body(), "", Parser.xmlParser());
            Elements items = doc.select("item");

            List<ArticleSaveRequest> articles = new ArrayList<>();

            // 4. 각 item 요소 순회
            for (Element item : items) {
                String title = item.selectFirst("title") != null ? item.selectFirst("title").text() : null;
                String link = item.selectFirst("link") != null ? item.selectFirst("link").text().trim() : null;
                String description = item.selectFirst("description") != null ? item.selectFirst("description").text() : "";
                String pubDate = item.selectFirst("pubDate") != null ? item.selectFirst("pubDate").text() : null;

                if (!containsKeyword(title, description, keywords)) continue;

                LocalDateTime publishedAt = parsePubDate(pubDate);

                // HTML 태그 제거
                String cleanTitle = HtmlCleaner.strip(title);
                String cleanDescription = HtmlCleaner.strip(description);

                // 키워드 하이라이팅 적용
                String highlightedTitle = highlightKeyword(cleanTitle, keywords);
                String highlightedDescription = highlightKeyword(cleanDescription, keywords);

                // ArticleSaveRequest 생성
                articles.add(new ArticleSaveRequest(
                    interestId,
                    sourceName(),
                    link,
                    highlightedTitle,
                    highlightedDescription != null ? highlightedDescription : "",
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
     * 제목 또는 요약(description)에 주어진 키워드 중 하나라도 포함되어 있는지 여부를 확인합니다.
     *
     * <p>대소문자를 구분하지 않으며, 키워드가 비어 있거나 null이면 항상 {@code true}를 반환합니다.
     * 제목 또는 요약이 null인 경우에도 빈 문자열로 처리하여 검사합니다.
     *
     * @param title 기사 제목 (null 허용)
     * @param description 기사 요약 또는 설명 (null 허용)
     * @param keywords 포함 여부를 확인할 키워드 리스트 (null 또는 빈 경우 전체 통과)
     * @return 제목 또는 설명에 하나 이상의 키워드가 포함되어 있으면 {@code true}, 그렇지 않으면 {@code false}
     */
    private boolean containsKeyword(String title, String description, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return true;

        String lowerTitle = title != null ? title.toLowerCase() : "";
        String lowerDesc = description != null ? description.toLowerCase() : "";

        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) continue;
            String lowerKeyword = keyword.toLowerCase();
            if (lowerTitle.contains(lowerKeyword) || lowerDesc.contains(lowerKeyword)) {
                return true;
            }
        }
        return false;
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
     * 텍스트 내에서 지정된 키워드들을 찾아 <strong> 태그로 감싸 볼드 처리합니다.
     *
     * <p>대소문자를 구분하지 않으며, 여러 키워드가 주어질 경우 모두 순차적으로 강조됩니다.
     *
     * <p>예: "AI is powerful" → 키워드 "ai" → 결과: "&lt;strong&gt;AI&lt;/strong&gt; is powerful"
     *
     * @param text 강조할 대상 문자열. {@code null} 이면 그대로 반환됩니다.
     * @param keywords 강조할 키워드 리스트. {@code null} 또는 비어 있으면 강조 없이 원문 반환합니다.
     * @return 키워드가 강조된 HTML 문자열
     */
    private String highlightKeyword(String text, List<String> keywords) {
        if (text == null || keywords == null || keywords.isEmpty()) return text;

        String result = text;
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) continue;

            String regex = "(?i)(" + Pattern.quote(keyword) + ")";
            result = result.replaceAll(regex, "<strong>$1</strong>");
        }
        return result;
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