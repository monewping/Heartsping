package org.project.monewping.domain.comment.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.domain.CommentLike;
import org.project.monewping.domain.comment.repository.CommentLikeRepository;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.project.monewping.domain.useractivity.service.UserActivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 좋아요 서비스
 * 사용자의 댓글 좋아요 등록 및 취소 비즈니스 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ArticlesRepository articlesRepository;
    private final UserActivityService userActivityService;
    public static final String RESOURCE_TYPE_COMMENT = "Comment";


    /**
     * 댓글 좋아요 등록
     * @param userId 사용자 ID
     * @param commentId 댓글 ID
     */
    public void likeComment(UUID userId, UUID commentId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (commentLikeRepository.existsByUserAndComment(user, comment)) {
            return; // 중복 좋아요 방지
        }

        CommentLike newLike = CommentLike.builder()
            .user(user)
            .comment(comment)
            .build();

        commentLikeRepository.save(newLike);
        comment.increaseLikeCount();

        addCommentLikeToUserActivity(userId, comment, newLike);
        createNotification(user.getNickname(), comment);
    }

    /**
     * 댓글 좋아요 취소
     *
     * @param userId 사용자 ID
     * @param commentId 댓글 ID
     */
    public void unlikeComment(UUID userId, UUID commentId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        commentLikeRepository.findByUserAndComment(user, comment)
            .ifPresent(commentLike -> {
                commentLikeRepository.delete(commentLike);
                comment.decreaseLikeCount();
            });

        // 사용자 활동 내역에서 댓글 좋아요 제거
        try {
            userActivityService.removeCommentLike(userId, commentId);
        } catch (Exception e) {
            // 활동 내역 업데이트 실패가 좋아요 취소 기능 자체를 실패시키지 않도록 예외를 잡아서 로그만 남김
            log.error("[CommentLikeService] 사용자 활동 내역 댓글 좋아요 제거 실패 - userId: {}, commentId: {}, error: {}",
                userId, commentId, e.getMessage(), e);
        }
    }

    private void addCommentLikeToUserActivity(UUID userId, Comment comment, CommentLike commentLike) {
        try {
            Articles article = articlesRepository.findById(comment.getArticleId()).orElse(null);

            if (article != null) {
                UserActivityDocument.CommentLikeInfo commentLikeInfo = UserActivityDocument.CommentLikeInfo.builder()
                    .id(commentLike.getId())
                    .createdAt(Instant.ofEpochMilli(commentLike.getCreatedAt().toEpochMilli()))
                    .commentId(comment.getId())
                    .articleId(article.getId())
                    .articleTitle(article.getTitle())
                    .commentUserId(comment.getUserId())
                    .commentUserNickname(comment.getUserNickname())
                    .commentContent(comment.getContent())
                    .commentLikeCount(comment.getLikeCount())
                    .commentCreatedAt(Instant.ofEpochMilli(comment.getCreatedAt().toEpochMilli()))
                    .build();

                userActivityService.addCommentLike(userId, commentLikeInfo);
            }
        } catch (Exception e) {
            log.error("[CommentLikeService] 사용자 활동 내역 댓글 좋아요 추가 실패 - userId: {}, commentId: {}, error: {}",
                userId, comment.getId(), e.getMessage(), e);
        }
    }

    /**
     * 댓글 좋아요 알림을 생성하여 저장합니다.
     *
     * <p>
     * 사용자가 댓글에 좋아요를 누르면 호출되는 메서드로,
     * "{user.nickname}님이 나의 댓글을 좋아합니다." 형태의 알림을 만들어 DB에 저장합니다.
     * </p>
     *
     * @param username 좋아요를 누른 사용자의 닉네임 (알림의 주체)
     * @param comment 좋아요 대상인 댓글 엔티티
     */
    private void createNotification(String username, Comment comment) {

        Notification notification = Notification.builder()
            .userId(comment.getUserId())
            .content(username + "님이 나의 댓글을 좋아합니다.")
            .resourceId(comment.getId())
            .resourceType(RESOURCE_TYPE_COMMENT)
            .confirmed(false)
            .active(true)
            .build();

        notificationRepository.save(notification);
        log.debug("생성된 알림: {}", notification);
    }
}