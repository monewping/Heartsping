package org.project.monewping.domain.article.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.interest.entity.Interest;

@DisplayName("ArticlesMapper 테스트")
public class ArticlesMapperTest {

    private ArticlesMapper articlesMapper;

    @BeforeEach
    void setUp() {
        articlesMapper = Mappers.getMapper(ArticlesMapper.class);
    }

    @Test
    @DisplayName("ArticleSaveRequest와 Interest를 Articles 엔티티로 매핑한다")
    void toEntity_shouldMapAllFields() {
        // Given: Interest 객체와 ArticleSaveRequest 객체가 주어졌을 때
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.builder()
            .id(interestId)
            .name("AI")
            .build();

        LocalDateTime publishedAt = LocalDateTime.now();

        ArticleSaveRequest request = new ArticleSaveRequest(
            interestId,
            "Naver",
            "https://naver.com/sample-article",
            "Sample Title",
            "Sample Summary",
            publishedAt
        );

        // When: ArticlesMapper를 사용해 Articles 엔티티로 변환하면
        Articles articles = articlesMapper.toEntity(request, interest);

        // Then: 모든 필드가 올바르게 매핑되었는지 확인한다
        assertThat(articles).isNotNull();
        assertThat(articles.getInterest()).isEqualTo(interest);
        assertThat(articles.getSource()).isEqualTo(request.source());
        assertThat(articles.getOriginalLink()).isEqualTo(request.originalLink());
        assertThat(articles.getTitle()).isEqualTo(request.title());
        assertThat(articles.getSummary()).isEqualTo(request.summary());
        assertThat(articles.getPublishedAt()).isEqualTo(request.publishedAt());

        // And: 기본값도 정상적으로 매핑되었는지 확인한다
        assertThat(articles.getViewCount()).isEqualTo(0);
        assertThat(articles.isDeleted()).isFalse();

        // And: commentCount 기본값 확인 (명시적 기본값 또는 null 허용 여부에 따라 수정 가능)
        assertThat(articles.getCommentCount()).isNotNull();
    }

}