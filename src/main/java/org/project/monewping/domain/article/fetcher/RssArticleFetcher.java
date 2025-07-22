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
     * 주어진 제목 또는 요약 중 하나라도 키워드를 포함하는지 확인합니다.
     * - 키워드가 null 또는 공백인 경우, 필터링 없이 true 반환
     * - 대소문자 구분 없이 검사합니다.
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
     * 주어진 텍스트에서 지정된 키워드를 찾아 하이라이팅 처리합니다.
     *
     * <p>키워드에 대해 대소문자를 구분하지 않고 검색하며,
     * 키워드에 포함된 특수문자는 정규식에서 안전하게 처리됩니다.
     * 하이라이팅은 {@code <span class="highlight">} 태그로 감싸서 적용합니다.</p>
     *
     * <p>예:</p>
     * <pre>
     * highlightKeyword("오늘은 사회 뉴스가 많습니다.", "사회")
     * → "오늘은 <span class="highlight">사회</span> 뉴스가 많습니다."
     * </pre>
     *
     * @param text    원본 텍스트 (예: 제목, 요약 등). {@code null}일 경우 {@code null} 반환
     * @param keywords 강조할 키워드. {@code null}이거나 빈 문자열인 경우 원본 텍스트 반환
     * @return 키워드가 하이라이팅 태그로 감싸진 문자열. 키워드가 없거나 조건에 맞지 않으면 원본 텍스트 그대로 반환
     */
    private String highlightKeyword(String text, List<String> keywords) {
        if (text == null || keywords == null || keywords.isEmpty()) return text;

        String result = text;
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) continue;

            // (?i) 대소문자 구분 없이, Pattern.quote로 특수문자 이스케이프 처리
            String regex = "(?i)(" + Pattern.quote(keyword) + ")";
            // 기존 하이라이트를 덮어쓰지 않게, 중복 하이라이트 방지 처리는 필요하면 추가 가능
            result = result.replaceAll(regex, "<span class=\"highlight\">$1</span>");
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