package org.project.monewping.domain.useractivity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 사용자 활동 내역 조회 응답 DTO
 * 
 * <p>
 * 사용자의 구독, 댓글, 좋아요, 기사 조회 활동 정보를 포함합니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDto {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID id;

    @NotNull(message = "사용자 이메일은 필수입니다.")
    private String email;

    @NotNull(message = "사용자 닉네임은 필수입니다.")
    private String nickname;

    @NotNull(message = "사용자 삭제 여부는 필수입니다.")
    private Boolean isDeleted;

    @NotNull(message = "가입 날짜는 필수입니다.")
    private Instant createdAt;

    private List<SubscriptionDto> subscriptions;

    private List<CommentDto> comments;

    private List<CommentLikeDto> commentLikes;

    private List<ArticleViewDto> articleViews;

    @NotNull(message = "마지막 업데이트 시간은 필수입니다.")
    private Instant updatedAt;

    /**
     * 구독 중인 관심사 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionDto {
        @NotNull(message = "구독 정보 ID는 필수입니다.")
        private UUID id;

        @NotNull(message = "관심사 ID는 필수입니다.")
        private UUID interestId;

        @NotNull(message = "관심사 이름은 필수입니다.")
        private String interestName;

        private List<String> interestKeywords;

        private long interestSubscriberCount;

        @NotNull(message = "구독 날짜는 필수입니다.")
        private Instant createdAt;
    }

    /**
     * 작성한 댓글 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {
        @NotNull(message = "댓글 ID는 필수입니다.")
        private UUID id;

        @NotNull(message = "기사 ID는 필수입니다.")
        private UUID articleId;

        @NotNull(message = "기사 제목은 필수입니다.")
        private String articleTitle;

        @NotNull(message = "작성자 ID는 필수입니다.")
        private UUID userId;

        @NotNull(message = "작성자 닉네임은 필수입니다.")
        private String userNickname;

        @NotNull(message = "댓글 내용은 필수입니다.")
        private String content;

        private long likeCount;

        @NotNull(message = "작성 날짜는 필수입니다.")
        private Instant createdAt;
    }

    /**
     * 좋아요를 누른 댓글 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentLikeDto {
        @NotNull(message = "좋아요 ID는 필수입니다.")
        private UUID id;

        @NotNull(message = "좋아요 날짜는 필수입니다.")
        private Instant createdAt;

        @NotNull(message = "댓글 ID는 필수입니다.")
        private UUID commentId;

        @NotNull(message = "기사 ID는 필수입니다.")
        private UUID articleId;

        @NotNull(message = "기사 제목은 필수입니다.")
        private String articleTitle;

        @NotNull(message = "댓글 작성자 ID는 필수입니다.")
        private UUID commentUserId;

        @NotNull(message = "댓글 작성자 닉네임은 필수입니다.")
        private String commentUserNickname;

        @NotNull(message = "댓글 내용은 필수입니다.")
        private String commentContent;

        private long commentLikeCount;

        @NotNull(message = "댓글 작성 날짜는 필수입니다.")
        private Instant commentCreatedAt;
    }

    /**
     * 조회한 기사 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticleViewDto {
        @NotNull(message = "조회 ID는 필수입니다.")
        private UUID id;

        @NotNull(message = "조회한 사용자 ID는 필수입니다.")
        private UUID viewedBy;

        @NotNull(message = "조회 날짜는 필수입니다.")
        private Instant createdAt;

        @NotNull(message = "기사 ID는 필수입니다.")
        private UUID articleId;

        @NotNull(message = "기사 출처는 필수입니다.")
        private String source;

        @NotNull(message = "기사 원본 URL은 필수입니다.")
        private String sourceUrl;

        @NotNull(message = "기사 제목은 필수입니다.")
        private String articleTitle;

        @NotNull(message = "기사 발행 날짜는 필수입니다.")
        private Instant articlePublishedDate;

        @NotNull(message = "기사 요약은 필수입니다.")
        private String articleSummary;

        private long articleCommentCount;

        private long articleViewCount;
    }
}
