package org.project.monewping.domain.article.fetcher;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 한국 경제 RSS 피드를 통해 뉴스 기사를 수집하는 컴포넌트입니다.
 * <p>
 * RSS XML 피드를 파싱하여 {@link ArticleSaveRequest} 형태로 변환합니다.
 * 수집된 기사 중 관심사 키워드가 제목 또는 요약에 포함된 경우에만 반환됩니다.
 * </p>
 */
@Slf4j
@Component
public class HankyungRssFetcher implements ArticleFetcher {

    private static final String RSS_FEED_URL = "https://www.hankyung.com/feed/all-news";

    private static final DateTimeFormatter PUBDATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    /**
     * 주어진 키워드를 기준으로 한국 경제 RSS에서 뉴스 기사를 수집합니다.
     *
     * @param keyword 관심사 키워드 (해당 키워드가 제목 또는 요약에 포함된 기사만 수집 대상)
     * @return 수집된 뉴스 기사 목록 ({@link ArticleSaveRequest})
     */
    @Override
    public List<ArticleSaveRequest> fetch(String keyword) {
        log.info("한국 경제 RSS 뉴스 수집 시작 = Keyword : {}",  keyword);

        try {
            // 1. HTTP GET 요청
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(RSS_FEED_URL))
                .GET()
                .build();

            // 응답 수신
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 실패 상태코드 확인
            if (response.statusCode() != 200) {
                log.error("한국 경제 RSS 피드 응답 실패 statusCode = {}", response.statusCode());
                return List.of();
            }

            // XML 본문 추출
            String xml = response.body();

            // 2. XML 파싱
            Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

            NodeList items = doc.getElementsByTagName("item");
            List<ArticleSaveRequest> articles = new ArrayList<>();

            for (int i = 0; i < items.getLength(); i++) {
                var item = items.item(i);

                // 태그 값 추출
                String title = getTagValue("title", item);
                String link = getTagValue("link", item);
                String description = getTagValue("description", item);
                String pubDate = getTagValue("pubDate", item);

                // 3. 발행일 파싱
                LocalDateTime publishedAt = LocalDateTime.now();
                if(pubDate != null && !pubDate.isEmpty()) {
                    try {
                        publishedAt = LocalDateTime.parse(pubDate, PUBDATE_FORMATTER);
                    } catch (Exception e) {
                        log.warn("발행일 파싱 실패, 기본값으로 대체 : {}", pubDate);
                    }
                }

                // 4. 제목 또는 요약에 키워드 포함 여부 확인
                if (keyword != null && !keyword.isEmpty()) {
                    String lowerKeyword = keyword.toLowerCase();
                    boolean containsKeyword = (title != null && title.toLowerCase().contains(lowerKeyword))
                        || (description != null && description.toLowerCase().contains(lowerKeyword));
                    // 키워드 미포함 시 해당 기사 제외
                    if (!containsKeyword) continue;
                }

                // 5. 수집 기사 객체 생성 및 저장
                articles.add(new ArticleSaveRequest(
                    // 관심사 ID는 외부 매핑
                    null,
                    "Naver",
                    link,
                    title,
                    // NPE 방지를 위해 Null이 아니면 그대로, Null이라면 빈 문자열로 대체
                    description != null ? description : "",
                    publishedAt
                ));
            }

            log.info("한국 경제 RSS 뉴스 수집 완료. 총 {}건", articles.size());
            return articles;

        } catch (Exception e) {
            log.error("한국 경제 RSS 뉴스 수집 중 예외 발생", e);
            return List.of();
        }

    }


    /**
     * XML 태그 이름을 기준으로 해당 노드에서 값을 추출합니다.
     *
     * @param tagName 태그 이름 (예: "title", "link")
     * @param item    RSS item 노드
     * @return 태그 값 또는 null
     */
    private String getTagValue(String tagName, org.w3c.dom.Node item) {
        var nodes = ((org.w3c.dom.Element) item).getElementsByTagName(tagName);
        if (nodes.getLength() > 0 && nodes.item(0).getFirstChild() != null) {
            return nodes.item(0).getFirstChild().getNodeValue();
        }
        return null;
    }
}
