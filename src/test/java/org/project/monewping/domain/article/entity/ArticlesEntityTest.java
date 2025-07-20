package org.project.monewping.domain.article.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.entity.Interest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Articles 엔티티 통합 테스트")
class ArticlesEntityTest {

    @Test
    @DisplayName("Articles 기본 생성자 테스트")
    void testDefaultConstructor() {
        // given & when
        Articles articles = new Articles();
        
        // then
        assertThat(articles).isNotNull();
        assertThat(articles.getId()).isNull();
        assertThat(articles.getTitle()).isNull();
        assertThat(articles.getSummary()).isNull();
        assertThat(articles.getOriginalLink()).isNull();
        assertThat(articles.getSource()).isNull();
        assertThat(articles.getPublishedAt()).isNull();
        assertThat(articles.getInterest()).isNull();
        assertThat(articles.getCommentCount()).isEqualTo(0L);
        assertThat(articles.getViewCount()).isEqualTo(0L);
        assertThat(articles.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Articles 생성자 테스트")
    void testConstructor() {
        // given
        String title = "테스트 제목";
        String summary = "테스트 요약";
        String originalLink = "https://test.com";
        String source = "테스트 소스";
        LocalDateTime publishedAt = LocalDateTime.now();
        Interest interest = Interest.builder()
                .name("테스트 관심사")
                .build();
        long commentCount = 10L;
        long viewCount = 100L;
        boolean deleted = false;
        long version = 1L;
        
        // when
        Articles articles = new Articles(interest, source, originalLink, title, summary, publishedAt, commentCount, viewCount, deleted, version);
        
        // then
        assertThat(articles.getTitle()).isEqualTo(title);
        assertThat(articles.getSummary()).isEqualTo(summary);
        assertThat(articles.getOriginalLink()).isEqualTo(originalLink);
        assertThat(articles.getSource()).isEqualTo(source);
        assertThat(articles.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(articles.getInterest()).isEqualTo(interest);
        assertThat(articles.getCommentCount()).isEqualTo(commentCount);
        assertThat(articles.getViewCount()).isEqualTo(viewCount);
        assertThat(articles.isDeleted()).isEqualTo(deleted);
        assertThat(articles.getVersion()).isEqualTo(version);
    }

    @Test
    @DisplayName("Articles Builder 패턴 테스트")
    void testBuilder() {
        // given
        String title = "빌더 테스트 제목";
        String summary = "빌더 테스트 요약";
        String originalLink = "https://builder-test.com";
        String source = "빌더 테스트 소스";
        LocalDateTime publishedAt = LocalDateTime.now();
        Interest interest = Interest.builder()
                .name("빌더 테스트 관심사")
                .build();
        long commentCount = 20L;
        long viewCount = 200L;
        boolean deleted = true;
        
        // when
        Articles articles = Articles.builder()
                .title(title)
                .summary(summary)
                .originalLink(originalLink)
                .source(source)
                .publishedAt(publishedAt)
                .interest(interest)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .deleted(deleted)
                .build();
        
        // then
        assertThat(articles.getTitle()).isEqualTo(title);
        assertThat(articles.getSummary()).isEqualTo(summary);
        assertThat(articles.getOriginalLink()).isEqualTo(originalLink);
        assertThat(articles.getSource()).isEqualTo(source);
        assertThat(articles.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(articles.getInterest()).isEqualTo(interest);
        assertThat(articles.getCommentCount()).isEqualTo(commentCount);
        assertThat(articles.getViewCount()).isEqualTo(viewCount);
        assertThat(articles.isDeleted()).isEqualTo(deleted);
    }

    @Test
    @DisplayName("Articles 엔티티 생성 및 저장 테스트")
    void testEntityCreationAndPersistence() {
        // given
        String title = "저장 테스트 제목";
        String summary = "저장 테스트 요약";
        String originalLink = "https://save-test.com";
        String source = "저장 테스트 소스";
        LocalDateTime publishedAt = LocalDateTime.now();
        Interest interest = Interest.builder()
                .name("저장 테스트 관심사")
                .build();
        long commentCount = 15L;
        long viewCount = 150L;
        boolean deleted = false;
        
        // when
        Articles articles = Articles.builder()
                .title(title)
                .summary(summary)
                .originalLink(originalLink)
                .source(source)
                .publishedAt(publishedAt)
                .interest(interest)
                .commentCount(commentCount)
                .viewCount(viewCount)
                .deleted(deleted)
                .build();
        
        // then - 엔티티가 올바르게 생성되었는지 확인
        assertThat(articles).isNotNull();
        assertThat(articles.getTitle()).isEqualTo(title);
        assertThat(articles.getSummary()).isEqualTo(summary);
        assertThat(articles.getOriginalLink()).isEqualTo(originalLink);
        assertThat(articles.getSource()).isEqualTo(source);
        assertThat(articles.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(articles.getInterest()).isEqualTo(interest);
        assertThat(articles.getCommentCount()).isEqualTo(commentCount);
        assertThat(articles.getViewCount()).isEqualTo(viewCount);
        assertThat(articles.isDeleted()).isEqualTo(deleted);
    }

    @Test
    @DisplayName("Articles 전체 필드 통합 테스트")
    void testAllFieldsIntegration() {
        // given
        Interest interest1 = Interest.builder().name("관심사1").build();
        Interest interest2 = Interest.builder().name("관심사2").build();
        
        // when
        Articles articles1 = Articles.builder()
                .title("제목1")
                .summary("요약1")
                .originalLink("https://test1.com")
                .source("소스1")
                .publishedAt(LocalDateTime.now())
                .interest(interest1)
                .commentCount(10L)
                .viewCount(100L)
                .deleted(false)
                .build();
        
        Articles articles2 = Articles.builder()
                .title("제목2")
                .summary("요약2")
                .originalLink("https://test2.com")
                .source("소스2")
                .publishedAt(LocalDateTime.now().plusDays(1))
                .interest(interest2)
                .commentCount(20L)
                .viewCount(200L)
                .deleted(true)
                .build();
        
        // then
        assertThat(articles1).isNotEqualTo(articles2);
        assertThat(articles1.getTitle()).isNotEqualTo(articles2.getTitle());
        assertThat(articles1.getInterest()).isNotEqualTo(articles2.getInterest());
        assertThat(articles1.getCommentCount()).isNotEqualTo(articles2.getCommentCount());
        assertThat(articles1.getViewCount()).isNotEqualTo(articles2.getViewCount());
        assertThat(articles1.isDeleted()).isNotEqualTo(articles2.isDeleted());
    }

    @Test
    @DisplayName("Articles 기본값 테스트")
    void testDefaultValues() {
        // given & when
        Articles articles = Articles.builder()
                .title("기본값 테스트")
                .summary("기본값 요약")
                .originalLink("https://default-test.com")
                .source("기본값 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("기본값 관심사").build())
                .build();
        
        // then
        assertThat(articles.getCommentCount()).isEqualTo(0L);
        assertThat(articles.getViewCount()).isEqualTo(0L);
        assertThat(articles.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Articles 다양한 데이터 타입 테스트")
    void testVariousDataTypes() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        LocalDateTime currentTime = LocalDateTime.now();
        
        // when
        Articles pastArticle = Articles.builder()
                .title("과거 기사")
                .summary("과거 요약")
                .originalLink("https://past.com")
                .source("과거 소스")
                .publishedAt(pastTime)
                .interest(Interest.builder().name("과거 관심사").build())
                .build();
        
        Articles futureArticle = Articles.builder()
                .title("미래 기사")
                .summary("미래 요약")
                .originalLink("https://future.com")
                .source("미래 소스")
                .publishedAt(futureTime)
                .interest(Interest.builder().name("미래 관심사").build())
                .build();
        
        Articles currentArticle = Articles.builder()
                .title("현재 기사")
                .summary("현재 요약")
                .originalLink("https://current.com")
                .source("현재 소스")
                .publishedAt(currentTime)
                .interest(Interest.builder().name("현재 관심사").build())
                .build();
        
        // then
        assertThat(pastArticle.getPublishedAt()).isBefore(currentTime);
        assertThat(futureArticle.getPublishedAt()).isAfter(currentTime);
        assertThat(currentArticle.getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Articles 긴 텍스트 테스트")
    void testLongTextFields() {
        // given
        String longTitle = "이것은 매우 긴 기사 제목입니다. ".repeat(10);
        String longSummary = "이것은 매우 긴 기사 요약입니다. ".repeat(50);
        String longSource = "매우긴소스명";
        
        // when
        Articles articles = Articles.builder()
                .title(longTitle)
                .summary(longSummary)
                .originalLink("https://long-text-test.com")
                .source(longSource)
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("긴 텍스트 관심사").build())
                .build();
        
        // then
        assertThat(articles.getTitle()).isEqualTo(longTitle);
        assertThat(articles.getSummary()).isEqualTo(longSummary);
        assertThat(articles.getSource()).isEqualTo(longSource);
    }

    @Test
    @DisplayName("Articles 특수 문자 테스트")
    void testSpecialCharacters() {
        // given
        String specialTitle = "특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        String specialSummary = "특수문자 요약: 한글, English, 123, !@#";
        String specialSource = "특수소스!@#";
        
        // when
        Articles articles = Articles.builder()
                .title(specialTitle)
                .summary(specialSummary)
                .originalLink("https://special-chars-test.com")
                .source(specialSource)
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("특수문자 관심사").build())
                .build();
        
        // then
        assertThat(articles.getTitle()).isEqualTo(specialTitle);
        assertThat(articles.getSummary()).isEqualTo(specialSummary);
        assertThat(articles.getSource()).isEqualTo(specialSource);
    }

    @Test
    @DisplayName("Articles 여러 기사 생성 테스트")
    void testMultipleArticles() {
        // given & when
        Articles[] articles = new Articles[5];
        for (int i = 0; i < 5; i++) {
            articles[i] = Articles.builder()
                    .title("기사 " + (i + 1))
                    .summary("요약 " + (i + 1))
                    .originalLink("https://article" + (i + 1) + ".com")
                    .source("소스" + (i + 1))
                    .publishedAt(LocalDateTime.now().plusDays(i))
                    .interest(Interest.builder().name("관심사" + (i + 1)).build())
                    .commentCount((long) (i + 1) * 10)
                    .viewCount((long) (i + 1) * 100)
                    .deleted(i % 2 == 0)
                    .build();
        }
        
        // then
        for (int i = 0; i < 5; i++) {
            assertThat(articles[i].getTitle()).isEqualTo("기사 " + (i + 1));
            assertThat(articles[i].getCommentCount()).isEqualTo((long) (i + 1) * 10);
            assertThat(articles[i].getViewCount()).isEqualTo((long) (i + 1) * 100);
            assertThat(articles[i].isDeleted()).isEqualTo(i % 2 == 0);
        }
    }

    @Test
    @DisplayName("Articles toString 메서드 테스트")
    void testToString() {
        // given
        Articles articles = Articles.builder()
                .title("toString 테스트")
                .summary("toString 요약")
                .originalLink("https://tostring-test.com")
                .source("toString 소스")
                .publishedAt(LocalDateTime.now())
                .interest(Interest.builder().name("toString 관심사").build())
                .build();
        
        // when
        String result = articles.toString();
        
        // then
        assertThat(result).isNotNull();
        // 기본 Object.toString()은 클래스명과 해시코드를 포함하므로 이를 확인
        assertThat(result).contains("Articles");
        assertThat(result).contains("@");
    }
} 