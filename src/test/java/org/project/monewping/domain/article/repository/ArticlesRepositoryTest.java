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
@DisplayName("ArticlesRepositoryCustom 쿼리 테스트")
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

}
