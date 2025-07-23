package org.project.monewping.domain.useractivity.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;

/**
 * UserActivityMapper의 단위 테스트
 * 
 * <p>
 * MapStruct로 생성된 매퍼의 Document ↔ DTO 변환 로직을 검증합니다.
 * </p>
 */
@DisplayName("UserActivityMapper 단위 테스트")
class UserActivityMapperTest {

    private UserActivityMapper mapper;
    private UUID testUserId;
    private Instant testCreatedAt;
    private Instant testUpdatedAt;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(UserActivityMapper.class);
        testUserId = UUID.randomUUID();
        testCreatedAt = Instant.now().minusSeconds(3600);
        testUpdatedAt = Instant.now();
    }

    @Test
    @DisplayName("UserActivityDocument를 UserActivityDto로 변환 성공")
    void toDto_Success() {
        // given
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("테스트유저")
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument.SubscriptionInfo subscription = UserActivityDocument.SubscriptionInfo.builder()
                .id(UUID.randomUUID())
                .interestId(UUID.randomUUID())
                .interestName("테스트 관심사")
                .interestKeywords(Arrays.asList("키워드1", "키워드2"))
                .interestSubscriberCount(100L)
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument.CommentInfo comment = UserActivityDocument.CommentInfo.builder()
                .id(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .articleTitle("테스트 기사")
                .userId(testUserId)
                .userNickname("테스트유저")
                .content("테스트 댓글")
                .likeCount(5L)
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument.CommentLikeInfo commentLike = UserActivityDocument.CommentLikeInfo.builder()
                .id(UUID.randomUUID())
                .commentId(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .articleTitle("좋아요한 기사")
                .commentUserId(UUID.randomUUID())
                .commentUserNickname("다른유저")
                .commentContent("좋아요한 댓글")
                .commentLikeCount(3L)
                .commentCreatedAt(testCreatedAt)
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument.ArticleViewInfo articleView = UserActivityDocument.ArticleViewInfo.builder()
                .id(UUID.randomUUID())
                .viewedBy(testUserId)
                .articleId(UUID.randomUUID())
                .source("테스트소스")
                .sourceUrl("https://test.com")
                .articleTitle("조회한 기사")
                .articlePublishedDate(testCreatedAt)
                .articleSummary("테스트 요약")
                .articleCommentCount(10L)
                .articleViewCount(50L)
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument document = UserActivityDocument.builder()
                .userId(testUserId)
                .user(userInfo)
                .subscriptions(Arrays.asList(subscription))
                .comments(Arrays.asList(comment))
                .commentLikes(Arrays.asList(commentLike))
                .articleViews(Arrays.asList(articleView))
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDto result = mapper.toDto(document);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNickname()).isEqualTo("테스트유저");
        assertThat(result.getCreatedAt()).isEqualTo(testCreatedAt);
        assertThat(result.getUpdatedAt()).isEqualTo(testUpdatedAt);

        // 구독 검증
        assertThat(result.getSubscriptions()).hasSize(1);
        UserActivityDto.SubscriptionDto resultSubscription = result.getSubscriptions().get(0);
        assertThat(resultSubscription.getInterestName()).isEqualTo("테스트 관심사");
        assertThat(resultSubscription.getInterestKeywords()).containsExactly("키워드1", "키워드2");
        assertThat(resultSubscription.getInterestSubscriberCount()).isEqualTo(100L);

        // 댓글 검증
        assertThat(result.getComments()).hasSize(1);
        UserActivityDto.CommentDto resultComment = result.getComments().get(0);
        assertThat(resultComment.getArticleTitle()).isEqualTo("테스트 기사");
        assertThat(resultComment.getContent()).isEqualTo("테스트 댓글");
        assertThat(resultComment.getLikeCount()).isEqualTo(5L);

        // 댓글 좋아요 검증
        assertThat(result.getCommentLikes()).hasSize(1);
        UserActivityDto.CommentLikeDto resultCommentLike = result.getCommentLikes().get(0);
        assertThat(resultCommentLike.getArticleTitle()).isEqualTo("좋아요한 기사");
        assertThat(resultCommentLike.getCommentContent()).isEqualTo("좋아요한 댓글");
        assertThat(resultCommentLike.getCommentLikeCount()).isEqualTo(3L);

        // 기사 조회 검증
        assertThat(result.getArticleViews()).hasSize(1);
        UserActivityDto.ArticleViewDto resultArticleView = result.getArticleViews().get(0);
        assertThat(resultArticleView.getArticleTitle()).isEqualTo("조회한 기사");
        assertThat(resultArticleView.getSource()).isEqualTo("테스트소스");
        assertThat(resultArticleView.getArticleViewCount()).isEqualTo(50L);
    }

    @Test
    @DisplayName("빈 컬렉션이 있는 UserActivityDocument 변환 성공")
    void toDto_WithEmptyCollections() {
        // given
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("테스트유저")
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument document = UserActivityDocument.builder()
                .userId(testUserId)
                .user(userInfo)
                .subscriptions(new ArrayList<>())
                .comments(new ArrayList<>())
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDto result = mapper.toDto(document);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNickname()).isEqualTo("테스트유저");
        assertThat(result.getSubscriptions()).isEmpty();
        assertThat(result.getComments()).isEmpty();
        assertThat(result.getCommentLikes()).isEmpty();
        assertThat(result.getArticleViews()).isEmpty();
    }

    @Test
    @DisplayName("null 컬렉션이 있는 UserActivityDocument 변환 성공")
    void toDto_WithNullCollections() {
        // given
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("테스트유저")
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument document = UserActivityDocument.builder()
                .userId(testUserId)
                .user(userInfo)
                .subscriptions(null)
                .comments(null)
                .commentLikes(null)
                .articleViews(null)
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDto result = mapper.toDto(document);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNickname()).isEqualTo("테스트유저");
        // null 컬렉션은 매퍼에서 어떻게 처리되는지에 따라 null 또는 빈 리스트일 수 있음
    }

    @Test
    @DisplayName("null UserActivityDocument 변환 시 null 반환")
    void toDto_WithNullDocument() {
        // when
        UserActivityDto result = mapper.toDto(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("사용자 정보만 있는 최소한의 Document 변환 성공")
    void toDto_MinimalDocument() {
        // given
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId)
                .email("minimal@example.com")
                .nickname("최소유저")
                .createdAt(testCreatedAt)
                .build();

        UserActivityDocument document = UserActivityDocument.builder()
                .userId(testUserId)
                .user(userInfo)
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDto result = mapper.toDto(document);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getEmail()).isEqualTo("minimal@example.com");
        assertThat(result.getNickname()).isEqualTo("최소유저");
        assertThat(result.getCreatedAt()).isEqualTo(testCreatedAt);
        assertThat(result.getUpdatedAt()).isEqualTo(testUpdatedAt);
    }

    @Test
    @DisplayName("복수개의 각 활동 타입이 있는 Document 변환 성공")
    void toDto_MultipleActivities() {
        // given
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId)
                .email("multi@example.com")
                .nickname("멀티유저")
                .createdAt(testCreatedAt)
                .build();

        List<UserActivityDocument.SubscriptionInfo> subscriptions = Arrays.asList(
                UserActivityDocument.SubscriptionInfo.builder()
                        .id(UUID.randomUUID())
                        .interestId(UUID.randomUUID())
                        .interestName("관심사1")
                        .createdAt(testCreatedAt)
                        .build(),
                UserActivityDocument.SubscriptionInfo.builder()
                        .id(UUID.randomUUID())
                        .interestId(UUID.randomUUID())
                        .interestName("관심사2")
                        .createdAt(testCreatedAt)
                        .build()
        );

        List<UserActivityDocument.CommentInfo> comments = Arrays.asList(
                UserActivityDocument.CommentInfo.builder()
                        .id(UUID.randomUUID())
                        .content("댓글1")
                        .likeCount(1L)
                        .createdAt(testCreatedAt)
                        .build(),
                UserActivityDocument.CommentInfo.builder()
                        .id(UUID.randomUUID())
                        .content("댓글2")
                        .likeCount(2L)
                        .createdAt(testCreatedAt)
                        .build()
        );

        UserActivityDocument document = UserActivityDocument.builder()
                .userId(testUserId)
                .user(userInfo)
                .subscriptions(subscriptions)
                .comments(comments)
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(testUpdatedAt)
                .build();

        // when
        UserActivityDto result = mapper.toDto(document);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSubscriptions()).hasSize(2);
        assertThat(result.getComments()).hasSize(2);
        assertThat(result.getCommentLikes()).isEmpty();
        assertThat(result.getArticleViews()).isEmpty();

        assertThat(result.getSubscriptions())
                .extracting(UserActivityDto.SubscriptionDto::getInterestName)
                .containsExactly("관심사1", "관심사2");

        assertThat(result.getComments())
                .extracting(UserActivityDto.CommentDto::getContent)
                .containsExactly("댓글1", "댓글2");
    }

    @Test
    @DisplayName("시간 정보가 정확히 매핑되는지 검증")
    void toDto_TimeMapping() {
        // given
        Instant specificTime = Instant.parse("2024-01-01T12:00:00Z");
        
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(testUserId)
                .email("time@example.com")
                .nickname("시간유저")
                .createdAt(specificTime)
                .build();

        UserActivityDocument document = UserActivityDocument.builder()
                .userId(testUserId)
                .user(userInfo)
                .subscriptions(new ArrayList<>())
                .comments(new ArrayList<>())
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(specificTime.plusSeconds(3600))
                .build();

        // when
        UserActivityDto result = mapper.toDto(document);

        // then
        assertThat(result.getCreatedAt()).isEqualTo(specificTime);
        assertThat(result.getUpdatedAt()).isEqualTo(specificTime.plusSeconds(3600));
    }
} 