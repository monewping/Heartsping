package org.project.monewping.domain.article.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.entity.Interest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ArticleViews 엔티티 통합 테스트")
class ArticleViewsEntityTest {

    @Test
    @DisplayName("ArticleViews 기본 생성자 테스트")
    void testDefaultConstructor() {
        // given & when
        ArticleViews articleViews = new ArticleViews();
        
        // then
        assertThat(articleViews).isNotNull();
        assertThat(articleViews.getId()).isNull();
        assertThat(articleViews.getViewedBy()).isNull();
        assertThat(articleViews.getArticle()).isNull();
        assertThat(articleViews.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("ArticleViews 생성자 테스트")
    void testConstructor() {
        // given
        UUID id = UUID.randomUUID();
        UUID viewedBy = UUID.randomUUID();
        Articles article = Articles.builder()
                .title("테스트 기사")
                .summary("테스트 요약")
                .originalLink("https://test.com")
                .source("테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("테스트 관심사").build())
                .build();
        LocalDateTime createdAt = LocalDateTime.now();
        
        // when
        ArticleViews articleViews = new ArticleViews(id, viewedBy, article, createdAt);
        
        // then
        assertThat(articleViews.getId()).isEqualTo(id);
        assertThat(articleViews.getViewedBy()).isEqualTo(viewedBy);
        assertThat(articleViews.getArticle()).isEqualTo(article);
        assertThat(articleViews.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("ArticleViews Builder 패턴 테스트")
    void testBuilder() {
        // given
        UUID id = UUID.randomUUID();
        UUID viewedBy = UUID.randomUUID();
        Articles article = Articles.builder()
                .title("빌더 테스트 기사")
                .summary("빌더 테스트 요약")
                .originalLink("https://builder-test.com")
                .source("빌더 테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("빌더 테스트 관심사").build())
                .build();
        LocalDateTime createdAt = LocalDateTime.now();
        
        // when
        ArticleViews articleViews = ArticleViews.builder()
                .id(id)
                .viewedBy(viewedBy)
                .article(article)
                .createdAt(createdAt)
                .build();
        
        // then
        assertThat(articleViews.getId()).isEqualTo(id);
        assertThat(articleViews.getViewedBy()).isEqualTo(viewedBy);
        assertThat(articleViews.getArticle()).isEqualTo(article);
        assertThat(articleViews.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("ArticleViews 엔티티 생성 및 저장 테스트")
    void testEntityCreationAndPersistence() {
        // given
        UUID viewedBy = UUID.randomUUID();
        Articles article = Articles.builder()
                .title("저장 테스트 기사")
                .summary("저장 테스트 요약")
                .originalLink("https://save-test.com")
                .source("저장 테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("저장 테스트 관심사").build())
                .build();
        LocalDateTime createdAt = LocalDateTime.now();
        
        // when
        ArticleViews articleViews = ArticleViews.builder()
                .viewedBy(viewedBy)
                .article(article)
                .createdAt(createdAt)
                .build();
        
        // then
        assertThat(articleViews).isNotNull();
        assertThat(articleViews.getViewedBy()).isEqualTo(viewedBy);
        assertThat(articleViews.getArticle()).isEqualTo(article);
        assertThat(articleViews.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("ArticleViews 전체 필드 통합 테스트")
    void testAllFieldsIntegration() {
        // given
        UUID viewedBy1 = UUID.randomUUID();
        UUID viewedBy2 = UUID.randomUUID();
        Articles article1 = Articles.builder()
                .title("기사1")
                .summary("요약1")
                .originalLink("https://article1.com")
                .source("소스1")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("관심사1").build())
                .build();
        Articles article2 = Articles.builder()
                .title("기사2")
                .summary("요약2")
                .originalLink("https://article2.com")
                .source("소스2")
                .publishedAt(LocalDateTime.now().plusDays(1))
                .interest(Interest.builder().name("관심사2").build())
                .build();
        LocalDateTime createdAt1 = LocalDateTime.now();
        LocalDateTime createdAt2 = LocalDateTime.now().plusHours(1);
        
        // when
        ArticleViews articleViews1 = ArticleViews.builder()
                .viewedBy(viewedBy1)
                .article(article1)
                .createdAt(createdAt1)
                .build();
        
        ArticleViews articleViews2 = ArticleViews.builder()
                .viewedBy(viewedBy2)
                .article(article2)
                .createdAt(createdAt2)
                .build();
        
        // then
        assertThat(articleViews1).isNotEqualTo(articleViews2);
        assertThat(articleViews1.getViewedBy()).isNotEqualTo(articleViews2.getViewedBy());
        assertThat(articleViews1.getArticle()).isNotEqualTo(articleViews2.getArticle());
        assertThat(articleViews1.getCreatedAt()).isNotEqualTo(articleViews2.getCreatedAt());
    }

    @Test
    @DisplayName("ArticleViews 여러 조회 생성 테스트")
    void testMultipleViews() {
        // given & when
        UUID userId = UUID.randomUUID();
        Articles article = Articles.builder()
                .title("여러 조회 테스트 기사")
                .summary("여러 조회 테스트 요약")
                .originalLink("https://multiple-views-test.com")
                .source("여러 조회 테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("여러 조회 테스트 관심사").build())
                .build();
        
        ArticleViews[] views = new ArticleViews[5];
        for (int i = 0; i < 5; i++) {
            views[i] = ArticleViews.builder()
                    .viewedBy(userId)
                    .article(article)
                    .createdAt(LocalDateTime.now().plusMinutes(i))
                    .build();
        }
        
        // then
        for (int i = 0; i < 5; i++) {
            assertThat(views[i].getViewedBy()).isEqualTo(userId);
            assertThat(views[i].getArticle()).isEqualTo(article);
            assertThat(views[i].getCreatedAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("ArticleViews 다양한 시간대 테스트")
    void testVariousTimeZones() {
        // given
        UUID userId = UUID.randomUUID();
        Articles article = Articles.builder()
                .title("시간대 테스트 기사")
                .summary("시간대 테스트 요약")
                .originalLink("https://timezone-test.com")
                .source("시간대 테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("시간대 테스트 관심사").build())
                .build();
        
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        
        // when
        ArticleViews pastView = ArticleViews.builder()
                .viewedBy(userId)
                .article(article)
                .createdAt(pastTime)
                .build();
        
        ArticleViews currentView = ArticleViews.builder()
                .viewedBy(userId)
                .article(article)
                .createdAt(currentTime)
                .build();
        
        ArticleViews futureView = ArticleViews.builder()
                .viewedBy(userId)
                .article(article)
                .createdAt(futureTime)
                .build();
        
        // then
        assertThat(pastView.getCreatedAt()).isBefore(currentTime);
        assertThat(futureView.getCreatedAt()).isAfter(currentTime);
        assertThat(currentView.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ArticleViews toString 메서드 테스트")
    void testToString() {
        // given
        UUID id = UUID.randomUUID();
        UUID viewedBy = UUID.randomUUID();
        Articles article = Articles.builder()
                .title("toString 테스트 기사")
                .summary("toString 테스트 요약")
                .originalLink("https://tostring-test.com")
                .source("toString 테스트 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("toString 테스트 관심사").build())
                .build();
        LocalDateTime createdAt = LocalDateTime.now();
        
        ArticleViews articleViews = ArticleViews.builder()
                .id(id)
                .viewedBy(viewedBy)
                .article(article)
                .createdAt(createdAt)
                .build();
        
        // when
        String result = articleViews.toString();
        
        // then
        assertThat(result).isNotNull();
        // 기본 Object.toString()은 클래스명과 해시코드를 포함하므로 이를 확인
        assertThat(result).contains("ArticleViews");
        assertThat(result).contains("@");
    }
} 