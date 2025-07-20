package org.project.monewping.domain.comment.exception;

/**
 * 댓글 좋아요가 존재하지 않을 때 발생하는 예외입니다.
 */
public class CommentLikeNotFoundException extends RuntimeException {

    public CommentLikeNotFoundException() {
    super("해당 댓글에 대한 좋아요가 존재하지 않습니다.");
  }
}