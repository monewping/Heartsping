package org.project.monewping.domain.comment.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.comment.domain.Comment;
import org.project.monewping.domain.comment.domain.CommentLike;
import org.project.monewping.domain.comment.exception.CommentLikeAlreadyExistsException;
import org.project.monewping.domain.comment.exception.CommentLikeNotFoundException;
import org.project.monewping.domain.comment.repository.CommentLikeRepository;
import org.project.monewping.domain.comment.repository.CommentRepository;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 댓글 좋아요 서비스
 * 사용자의 댓글 좋아요 등록 및 취소 비즈니스 로직을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

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
}
