package org.project.monewping.domain.article.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.entity.Interest;

@DisplayName("Articles 엔티티 테스트")
class ArticlesTest {

    private Interest interest;
    private Articles articles;

    @BeforeEach
    void setUp() {
        interest = Interest.builder()
            .name("테스트 관심사")
            .build();

        articles = Articles.builder()
            .interest(interest)
            .source("테스트 소스")
            .originalLink("https://test.com/article/1")
            .title("테스트 기사 제목")
            .summary("테스트 기사 요약")
            .publishedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .commentCount(10L)
            .viewCount(100L)
            .deleted(false)
            .build();
    }

    @Test
    @DisplayName("Articles 생성자 테스트")
    void testConstructor() {
        // given
        Interest testInterest = Interest.builder().name("새 관심사").build();
        LocalDateTime publishedAt = LocalDateTime.of(2025, 1, 1, 12, 0);

        // when
        Articles newArticles = new Articles(
                testInterest,
                "새 소스",
                "https://new.com/article/1",
                "새 기사 제목",
                "새 기사 요약",
                publishedAt,
                5L,
                50L,
                false
        );

        // then
        assertThat(newArticles.getInterest()).isEqualTo(testInterest);
        assertThat(newArticles.getSource()).isEqualTo("새 소스");
        assertThat(newArticles.getOriginalLink()).isEqualTo("https://new.com/article/1");
        assertThat(newArticles.getTitle()).isEqualTo("새 기사 제목");
        assertThat(newArticles.getSummary()).isEqualTo("새 기사 요약");
        assertThat(newArticles.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(newArticles.getCommentCount()).isEqualTo(5L);
        assertThat(newArticles.getViewCount()).isEqualTo(50L);
        assertThat(newArticles.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Builder 패턴 테스트")
    void testBuilder() {
        // given & when
        Articles builtArticles = Articles.builder()
            .interest(interest)
            .source("빌더 소스")
            .originalLink("https://builder.com/article/1")
            .title("빌더로 생성된 기사")
            .summary("빌더로 생성된 요약")
            .publishedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .commentCount(20L)
            .viewCount(200L)
            .deleted(true)
            .build();

        // then
        assertThat(builtArticles.getInterest()).isEqualTo(interest);
        assertThat(builtArticles.getSource()).isEqualTo("빌더 소스");
        assertThat(builtArticles.getOriginalLink()).isEqualTo("https://builder.com/article/1");
        assertThat(builtArticles.getTitle()).isEqualTo("빌더로 생성된 기사");
        assertThat(builtArticles.getSummary()).isEqualTo("빌더로 생성된 요약");
        assertThat(builtArticles.getPublishedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(builtArticles.getCommentCount()).isEqualTo(20L);
        assertThat(builtArticles.getViewCount()).isEqualTo(200L);
        assertThat(builtArticles.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("기본값 테스트")
    void testDefaultValues() {
        // given & when
        Articles defaultArticles = new Articles();

        // then
        assertThat(defaultArticles.getInterest()).isNull();
        assertThat(defaultArticles.getSource()).isNull();
        assertThat(defaultArticles.getOriginalLink()).isNull();
        assertThat(defaultArticles.getTitle()).isNull();
        assertThat(defaultArticles.getSummary()).isNull();
        assertThat(defaultArticles.getPublishedAt()).isNull();
        assertThat(defaultArticles.getCommentCount()).isEqualTo(0L);
        assertThat(defaultArticles.getViewCount()).isEqualTo(0L);
        assertThat(defaultArticles.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Interest 연관관계 테스트")
    void testInterestRelationship() {
        // given
        Interest newInterest = Interest.builder()
            .name("새로운 관심사")
            .build();

        // when
        articles = Articles.builder()
            .interest(newInterest)
            .source("테스트")
            .originalLink("https://test.com")
            .title("테스트")
            .summary("테스트")
            .publishedAt(LocalDateTime.now())
            .build();

        // then
        assertThat(articles.getInterest()).isEqualTo(newInterest);
        assertThat(articles.getInterest().getName()).isEqualTo("새로운 관심사");
    }

    @Test
    @DisplayName("기사 정보 설정 테스트")
    void testArticleInformation() {
        // given
        String newTitle = "새로운 기사 제목";
        String newSummary = "새로운 기사 요약";
        String newSource = "새로운 소스";
        String newLink = "https://new.com/article/2";

        // when
        articles = Articles.builder()
            .interest(interest)
            .source(newSource)
            .originalLink(newLink)
            .title(newTitle)
            .summary(newSummary)
            .publishedAt(LocalDateTime.of(2025, 1, 1, 12, 0))
            .build();

        // then
        assertThat(articles.getTitle()).isEqualTo(newTitle);
        assertThat(articles.getSummary()).isEqualTo(newSummary);
        assertThat(articles.getSource()).isEqualTo(newSource);
        assertThat(articles.getOriginalLink()).isEqualTo(newLink);
    }

    @Test
    @DisplayName("통계 정보 테스트")
    void testStatistics() {
        // given
        long commentCount = 15L;
        long viewCount = 150L;

        // when
        articles = Articles.builder()
            .interest(interest)
            .source("테스트")
            .originalLink("https://test.com")
            .title("테스트")
            .summary("테스트")
            .publishedAt(LocalDateTime.now())
            .commentCount(commentCount)
            .viewCount(viewCount)
            .build();

        // then
        assertThat(articles.getCommentCount()).isEqualTo(commentCount);
        assertThat(articles.getViewCount()).isEqualTo(viewCount);
    }

    @Test
    @DisplayName("삭제 상태 테스트")
    void testDeletedStatus() {
        // given
        boolean deletedStatus = true;

        // when
        articles = Articles.builder()
            .interest(interest)
            .source("테스트")
            .originalLink("https://test.com")
            .title("테스트")
            .summary("테스트")
            .publishedAt(LocalDateTime.now())
            .deleted(deletedStatus)
            .build();

        // then
        assertThat(articles.isDeleted()).isEqualTo(deletedStatus);
    }

    @Test
    @DisplayName("발행일 테스트")
    void testPublishedAt() {
        // given
        LocalDateTime publishedAt = LocalDateTime.of(2025, 12, 31, 23, 59);

        // when
        articles = Articles.builder()
            .interest(interest)
            .source("테스트")
            .originalLink("https://test.com")
            .title("테스트")
            .summary("테스트")
            .publishedAt(publishedAt)
            .build();

        // then
        assertThat(articles.getPublishedAt()).isEqualTo(publishedAt);
    }

    @Test
    @DisplayName("전체 필드 테스트")
    void testAllFields() {
        // given
        Interest testInterest = Interest.builder().name("전체 테스트 관심사").build();
        LocalDateTime publishedAt = LocalDateTime.of(2025, 6, 15, 14, 30);

        // when
        Articles fullArticles = Articles.builder()
            .interest(testInterest)
            .source("전체 테스트 소스")
            .originalLink("https://fulltest.com/article/999")
            .title("전체 테스트 기사 제목")
            .summary("전체 테스트 기사 요약 내용입니다.")
            .publishedAt(publishedAt)
            .commentCount(999L)
            .viewCount(9999L)
            .deleted(false)
            .build();

        // then
        assertThat(fullArticles.getInterest()).isEqualTo(testInterest);
        assertThat(fullArticles.getSource()).isEqualTo("전체 테스트 소스");
        assertThat(fullArticles.getOriginalLink()).isEqualTo("https://fulltest.com/article/999");
        assertThat(fullArticles.getTitle()).isEqualTo("전체 테스트 기사 제목");
        assertThat(fullArticles.getSummary()).isEqualTo("전체 테스트 기사 요약 내용입니다.");
        assertThat(fullArticles.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(fullArticles.getCommentCount()).isEqualTo(999L);
        assertThat(fullArticles.getViewCount()).isEqualTo(9999L);
        assertThat(fullArticles.isDeleted()).isFalse();
    }
} 