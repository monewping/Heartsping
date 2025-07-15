package org.project.monewping.domain.comment.exception;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {

  public CommentNotFoundException(UUID commentId) {
    super("댓글을 찾을 수 없습니다. ID: " + commentId);
  }
}
