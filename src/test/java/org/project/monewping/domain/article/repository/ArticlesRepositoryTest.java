package org.project.monewping.domain.article.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@DisplayName("ArticlesRepository 테스트")
public class ArticlesRepositoryTest {

    @Autowired
    private ArticlesRepository articlesRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Test
    @DisplayName("관심사, 출처, 날짜 범위 필터와 함께 기사 목록을 조회할 수 있다")
    void searchArticles_withFiltersAndSortingAndCursor() {
        // given: 테스트 준비 - 관심사 및 기사 엔티티 생성 및 저장
        Interest interest = Interest.builder()
            .name("테스트 관심사")
            .subscriberCount(0)
            .build();
        interest = interestRepository.saveAndFlush(interest);  // 영속 상태 보장

        Articles article = Articles.builder()
            .interest(interest)
            .source("연합뉴스")
            .originalLink("https://news.com/article1")
            .title("AI 산업 동향")
            .summary("요약")
            .publishedAt(LocalDateTime.now().minusDays(1))
            .build();
        articlesRepository.save(article);
        articlesRepository.flush();

        ArticleSearchRequest request = new ArticleSearchRequest(
            "AI",
            interest.getId(),
            List.of("연합뉴스"),
            LocalDateTime.now().minusDays(7),
            LocalDateTime.now(),
            "publishDate",
            "DESC",
            null,
            null,
            10,
            null
        );

        // when: 실제 테스트 대상 메서드 호출
        List<Articles> results = articlesRepository.searchArticles(request);

        // then: 결과 검증
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getInterest().getId()).isEqualTo(interest.getId());
        assertThat(results.get(0).getSource()).isEqualTo("연합뉴스");
        assertThat(results.get(0).getTitle()).contains("AI");
    }

    @Test
    @DisplayName("댓글 수 기준으로 내림차순 정렬된 기사 목록을 조회할 수 있다")
    void searchArticles_sortByCommentCountDesc() {
        // given
        Interest interest = interestRepository.save(
            Interest.builder().name("IT").subscriberCount(0).build()
        );

        Articles article1 = Articles.builder()
            .interest(interest)
            .source("조선일보")
            .originalLink("https://news.com/1")
            .title("ChatGPT 열풍")
            .summary("요약1")
            .publishedAt(LocalDateTime.now())
            .commentCount(10L)
            .build();

        Articles article2 = Articles.builder()
            .interest(interest)
            .source("조선일보")
            .originalLink("https://news.com/2")
            .title("AI 산업의 미래")
            .summary("요약2")
            .publishedAt(LocalDateTime.now().minusHours(1))
            .commentCount(30L)
            .build();

        articlesRepository.saveAll(List.of(article1, article2));
        articlesRepository.flush();

        ArticleSearchRequest request = new ArticleSearchRequest(
            null,
            null,
            null,
            null,
            null,
            "commentCount",
            "DESC",
            null,
            null,
            10,
            null
        );

        // when
        List<Articles> results = articlesRepository.searchArticles(request);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCommentCount()).isEqualTo(30L);
        assertThat(results.get(1).getCommentCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("필터 조건에 맞는 전체 기사의 개수를 정확히 반환한다")
    void countArticles_returnsExactCount() {
        // given
        Interest interest = interestRepository.save(
            Interest.builder().name("경제").subscriberCount(0).build()
        );

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("한국경제")
            .originalLink("https://news.com/eco1")
            .title("경제 성장률 상승")
            .summary("요약")
            .publishedAt(LocalDateTime.now())
            .build());

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("한국경제")
            .originalLink("https://news.com/eco2")
            .title("경제 불황 진입")
            .summary("요약")
            .publishedAt(LocalDateTime.now())
            .build());

        ArticleSearchRequest request = new ArticleSearchRequest(
            "경제",
            interest.getId(),
            List.of("한국경제"),
            null,
            null,
            null,
            null,
            null,
            null,
            10,
            null
        );

        // when
        long count = articlesRepository.countArticles(request);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("날짜 필터 없이 전체 기간의 기사를 조회할 수 있다")
    void searchArticles_withoutDateFilter() {
        // given
        Interest interest = interestRepository.save(
            Interest.builder().name("문화").subscriberCount(0).build()
        );

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("한겨레")
            .originalLink("https://news.com/m1")
            .title("뮤지컬 흥행")
            .summary("요약1")
            .publishedAt(LocalDateTime.now().minusDays(30))
            .build());

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("한겨레")
            .originalLink("https://news.com/m2")
            .title("공연 취소")
            .summary("요약2")
            .publishedAt(LocalDateTime.now().minusDays(1))
            .build());

        ArticleSearchRequest request = new ArticleSearchRequest(
            null,
            interest.getId(),
            List.of("한겨레"),
            null,
            null,
            "publishDate",
            "ASC",
            null,
            null,
            10,
            null
        );

        // when
        List<Articles> results = articlesRepository.searchArticles(request);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getPublishedAt()).isBefore(results.get(1).getPublishedAt());
    }

    @Test
    @DisplayName("이전 ID 기준으로 커서 페이지네이션 조회가 가능하다")
    void searchArticles_withCursorBeforeId() {
        // given
        Interest interest = interestRepository.save(
            Interest.builder().name("정치").subscriberCount(0).build()
        );

        LocalDateTime baseTime = LocalDateTime.of(2025, 7, 17, 12, 0);

        Articles a1 = articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("중앙일보")
            .originalLink("https://news.com/p1")
            .title("정치 개혁")
            .summary("요약1")
            .publishedAt(baseTime.minusMinutes(3))  // 11:57
            .build());

        Articles a2 = articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("중앙일보")
            .originalLink("https://news.com/p2")
            .title("총선 결과")
            .summary("요약2")
            .publishedAt(baseTime.minusMinutes(2))  // 11:58
            .build());

        Articles a3 = articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("중앙일보")
            .originalLink("https://news.com/p3")
            .title("여야 협상")
            .summary("요약3")
            .publishedAt(baseTime.minusMinutes(1))  // 11:59
            .build());

        articlesRepository.flush();

        ArticleSearchRequest request = new ArticleSearchRequest(
            null,
            interest.getId(),
            List.of("중앙일보"),
            null,
            null,
            "publishDate",
            "DESC",
            a2.getId().toString(),
            a2.getPublishedAt(),
            10,
            null
        );

        // when
        List<Articles> results = articlesRepository.searchArticles(request);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(a1.getId());
    }

    @Test
    @DisplayName("삭제되지 않은 기사들의 출처 목록만 조회된다")
    void findDistinctSources_excludesDeletedArticles() {
        // given
        Interest interest = interestRepository.save(
            Interest.builder()
                .name("정치")
                .subscriberCount(0)
                .build()
        );

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("중앙일보")
            .originalLink("https://news.com/1")
            .title("뉴스1")
            .summary("요약1")
            .publishedAt(LocalDateTime.now())
            .deleted(false)
            .build());

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("한겨레")
            .originalLink("https://news.com/2")
            .title("뉴스2")
            .summary("요약2")
            .publishedAt(LocalDateTime.now())
            .deleted(true) // 삭제된 기사
            .build());

        // when
        List<String> sources = articlesRepository.findDistinctSources();

        // then
        assertThat(sources).containsExactly("중앙일보");
    }

    @Test
    @DisplayName("중복된 출처는 하나만 반환된다")
    void findDistinctSources_returnsUniqueSources() {
        // given
        Interest interest = interestRepository.save(
            Interest.builder()
                .name("경제")
                .subscriberCount(0)
                .build()
        );

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("연합뉴스")
            .originalLink("https://news.com/a")
            .title("뉴스A")
            .summary("요약A")
            .publishedAt(LocalDateTime.now())
            .deleted(false)
            .build());

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("연합뉴스") // 중복 출처
            .originalLink("https://news.com/b")
            .title("뉴스B")
            .summary("요약B")
            .publishedAt(LocalDateTime.now())
            .deleted(false)
            .build());

        // when
        List<String> sources = articlesRepository.findDistinctSources();

        // then
        assertThat(sources).containsExactly("연합뉴스");
    }

    @Test
    @DisplayName("삭제되지 않은 기사가 없는 경우 빈 리스트를 반환한다")
    void findDistinctSources_returnsEmptyListIfNoArticles() {
        // given
        Interest interest = interestRepository.save(
            Interest.builder()
                .name("스포츠")
                .subscriberCount(0)
                .build()
        );

        articlesRepository.save(Articles.builder()
            .interest(interest)
            .source("KBS")
            .originalLink("https://news.com/z")
            .title("뉴스Z")
            .summary("요약Z")
            .publishedAt(LocalDateTime.now())
            .deleted(true) // 모두 삭제된 상태
            .build());

        // when
        List<String> sources = articlesRepository.findDistinctSources();

        // then
        assertThat(sources).isEmpty();
    }

}
