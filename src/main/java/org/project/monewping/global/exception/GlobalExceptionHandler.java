package org.project.monewping.global.exception;

import java.time.Instant;
import org.project.monewping.domain.comment.exception.CommentDeleteException;
import org.project.monewping.domain.comment.exception.CommentNotFoundException;
import org.project.monewping.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러
 * <p>
 * Controller 계층에서 발생하는 도메인 예외를 공통으로 처리하고
 * HTTP 상태 코드와 에러 메시지를 일관된 형태로 반환한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 댓글 삭제 예외 처리
   * <p>
   * 본인이 작성하지 않은 댓글을 삭제 시도할 경우 403 Forbidden 응답을 반환한다.
   *
   * @param ex CommentDeleteException
   * @return 403 Forbidden 에러 응답
   */
    @ExceptionHandler(CommentDeleteException.class)
    public ResponseEntity<ErrorResponse> handleCommentDeleteException(CommentDeleteException ex) {
        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

  /**
   * 댓글 조회 실패 예외 처리
   * <p>
   * 존재하지 않는 댓글 ID로 삭제 또는 조회 시도할 경우 404 Not Found 응답을 반환한다.
   *
   * @param ex CommentNotFoundException
   * @return 404 Not Found 에러 응답
   */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFoundException(CommentNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}