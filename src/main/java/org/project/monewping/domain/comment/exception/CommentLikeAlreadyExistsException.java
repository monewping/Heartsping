package org.project.monewping.domain.comment.exception;

/**
 * 댓글 좋아요가 이미 존재할 때 발생하는 예외입니다.
 */
public class CommentLikeAlreadyExistsException extends RuntimeException {

    public CommentLikeAlreadyExistsException() {
        super("이미 해당 댓글에 좋아요를 누른 상태입니다.");
    }

}