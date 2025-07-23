package org.project.monewping.domain.comment.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.domain.CommentLike;
import org.project.monewping.domain.comment.exception.CommentLikeAlreadyExistsException;
import org.project.monewping.domain.comment.exception.CommentLikeNotFoundException;
import org.project.monewping.domain.comment.repository.CommentLikeRepository;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
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
          throw new CommentLikeAlreadyExistsException();
        }

        commentLikeRepository.save(
            CommentLike.builder()
                .user(user)
                .comment(comment)
                .build()
        );

        createNotification(user.getNickname(), comment);
  }

  /**
   * 댓글 좋아요 취소
   * @param userId 사용자 ID
   * @param commentId 댓글 ID
   */
    public void unlikeComment(UUID userId, UUID commentId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        CommentLike commentLike = commentLikeRepository.findByUserAndComment(user, comment)
            .orElseThrow(CommentLikeNotFoundException::new);

        commentLikeRepository.delete(commentLike);
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
            .resourceType("Comment")
            .confirmed(false)
            .active(true)
            .build();

        notificationRepository.save(notification);
        log.debug("생성된 알림: {}", notification);
    }
}