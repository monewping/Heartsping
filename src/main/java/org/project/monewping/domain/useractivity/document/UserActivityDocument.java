package org.project.monewping.domain.useractivity.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 사용자 활동 내역을 저장하는 MongoDB Document
 * 
 * <p>
 * 사용자의 구독, 댓글, 좋아요, 기사 조회 활동을 역정규화하여 저장합니다.
 * 조회 성능 최적화를 위해 MongoDB를 사용합니다.
 * </p>
 */
@Document(collection = "user_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDocument {

    @Id
    private UUID userId;

    @Field("user")
    private UserInfo user;

    @Field("subscriptions")
    private List<SubscriptionInfo> subscriptions;

    @Field("comments")
    private List<CommentInfo> comments;

    @Field("comment_likes")
    private List<CommentLikeInfo> commentLikes;

    @Field("article_views")
    private List<ArticleViewInfo> articleViews;

    @Field("updated_at")
    private Instant updatedAt;

    /**
     * 사용자 기본 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String nickname;
        private Instant createdAt;
    }

    /**
     * 구독 중인 관심사 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private UUID id;
        private UUID interestId;
        private String interestName;
        private List<String> interestKeywords;
        private long interestSubscriberCount;
        private Instant createdAt;
    }

    /**
     * 작성한 댓글 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentInfo {
        private UUID id;
        private UUID articleId;
        private String articleTitle;
        private UUID userId;
        private String userNickname;
        private String content;
        private long likeCount;
        private Instant createdAt;
    }

    /**
     * 좋아요를 누른 댓글 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentLikeInfo {
        private UUID id;
        private Instant createdAt;
        private UUID commentId;
        private UUID articleId;
        private String articleTitle;
        private UUID commentUserId;
        private String commentUserNickname;
        private String commentContent;
        private long commentLikeCount;
        private Instant commentCreatedAt;
    }

    /**
     * 조회한 기사 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleViewInfo {
        private UUID id;
        private UUID viewedBy;
        private Instant createdAt;
        private UUID articleId;
        private String source;
        private String sourceUrl;
        private String articleTitle;
        private Instant articlePublishedDate;
        private String articleSummary;
        private long articleCommentCount;
        private long articleViewCount;
    }
}
