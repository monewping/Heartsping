package org.project.monewping.global.exception;

import java.time.Instant;
import java.util.stream.Collectors;
import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.project.monewping.domain.comment.exception.CommentDeleteException;
import org.project.monewping.domain.comment.exception.CommentNotFoundException;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.InterestNotFoundException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.project.monewping.domain.notification.exception.InvalidCursorFormatException;
import org.project.monewping.domain.notification.exception.NotificationNotFoundException;
import org.project.monewping.domain.notification.exception.UnsupportedResourceTypeException;
import org.project.monewping.domain.notification.exception.NotificationBatchRunException;
import org.project.monewping.domain.user.exception.UserNotFoundException;
import org.project.monewping.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리를 담당하는 컨트롤러 어드바이스
 *
 * <p>
 * 애플리케이션 전체에서 발생하는 예외를 일관된 형태로 처리하여
 * 클라이언트에게 적절한 HTTP 상태 코드와 오류 메시지를 제공합니다.
 * </p>
 *
 * <p>
 * 처리하는 예외 유형:
 * </p>
 * <ul>
 * <li>{@link MethodArgumentNotValidException} - Bean Validation 실패</li>
 * <li>{@link EmailAlreadyExistsException} - 이메일 중복</li>
 * <li>{@link Exception} - 기타 예외</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateInterestNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateInterestNameException(DuplicateInterestNameException e) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.CONFLICT, "DUPLICATE_INTEREST_NAME", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(SimilarInterestNameException.class)
    public ResponseEntity<ErrorResponse> handleSimilarInterestNameException(SimilarInterestNameException e) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.CONFLICT, "SIMILAR_INTEREST_NAME", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InterestCreationException.class)
    public ResponseEntity<ErrorResponse> handleInterestCreationException(InterestCreationException e) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTEREST_CREATION_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(InterestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInterestNotFoundException(InterestNotFoundException e) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.NOT_FOUND, "INTEREST_NOT_FOUND", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Bean Validation 실패 시 예외를 처리합니다.
     *
     * <p>
     *
     * @Valid 애노테이션을 사용한 유효성 검사 실패 시 발생하는 예외를 처리하여
     *        400 Bad Request 상태 코드와 함께 상세한 오류 메시지를 반환합니다.
     *        </p>
     *
     * @param ex 유효성 검사 실패 예외
     * @return 400 Bad Request 상태와 오류 정보를 포함한 ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                return fieldName + ": " + errorMessage;
            })
            .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "유효성 검사 실패",
            errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 지원하지 않는 리소스 타입 예외를 처리합니다.
     *
     * <p>UnsupportedResourceTypeException이 발생하면 이 메서드가 호출되어
     * HTTP 400 Bad Request 상태 코드와 함께 에러 정보를 담은 ErrorResponse를 반환합니다.</p>
     *
     * @param ex 발생한 UnsupportedResourceTypeException
     * @return HTTP 400 상태와 에러 메시지를 담은 ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(UnsupportedResourceTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedResourceTypeException(
        UnsupportedResourceTypeException ex
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 이메일 중복 예외를 처리합니다.
     *
     * <p>
     * 회원가입 시 동일한 이메일을 가진 사용자가 이미 존재하는 경우 발생하는 예외를 처리하여
     * 409 Conflict 상태 코드와 함께 오류 메시지를 반환합니다.
     * </p>
     *
     * @param ex 이메일 중복 예외
     * @return 409 Conflict 상태와 오류 정보를 포함한 ResponseEntity
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
        EmailAlreadyExistsException ex) {

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * JSON 파싱 오류를 처리합니다.
     *
     * <p>
     * 잘못된 JSON 형식으로 인해 발생하는 예외를 처리하여
     * 400 Bad Request 상태 코드와 함께 오류 메시지를 반환합니다.
     * </p>
     *
     * @param ex JSON 파싱 예외
     * @return 400 Bad Request 상태와 오류 정보를 포함한 ResponseEntity
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex) {

        String message = "잘못된 JSON 형식입니다.";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "JSON 파싱 오류: 올바른 JSON 형식으로 요청해주세요.";
        }

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            message,
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 알림을 찾을 수 없을 때 발생하는 예외를 처리합니다.
     *
     * <p>NotificationNotFoundException이 발생하면 이 메서드가 호출되어
     * HTTP 404 Not Found 상태 코드와 함께 에러 정보를 담은 ErrorResponse를 반환합니다.</p>
     *
     * @param ex 발생한 NotificationNotFoundException
     * @return HTTP 404 상태와 에러 메시지를 담은 ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFoundException(
        NotificationNotFoundException ex
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 사용자를 조회할 수 없을 때 발생하는 예외를 처리합니다.
     *
     * @param ex 발생한 UserNotFoundException
     * @return HTTP 404 상태와 에러 메시지를 담은 ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND,
            "사용자를 조회할 수 없습니다.",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 기타 예외를 처리합니다.
     *
     * <p>
     * 명시적으로 처리되지 않은 모든 예외를 처리하여
     * 500 Internal Server Error 상태 코드와 함께 일반적인 오류 메시지를 반환합니다.
     * </p>
     *
     * @param ex 처리되지 않은 예외
     * @return 500 Internal Server Error 상태와 오류 정보를 포함한 ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다.",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 중복 기사 조회 예외 처리 (409 Conflict).
     */
    @ExceptionHandler(DuplicateArticleViewsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateArticleView(DuplicateArticleViewsException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.CONFLICT,
            "이미 조회한 기사입니다.",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * UUID 형식 오류 등 잘못된 요청 파라미터 예외 처리 (400 Bad Request).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "잘못된 요청 형식입니다.",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 필수 요청 헤더 누락 예외 처리 (400 Bad Request).
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "필수 요청 헤더가 누락되었습니다.",
            "누락된 헤더 이름: " + ex.getHeaderName()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 필수 요청 파라미터가 누락되었을 때 발생하는 예외를 처리합니다.
     * 클라이언트에 누락된 파라미터 이름과 함께 400 Bad Request 상태 코드를 반환합니다.
     *
     * @param ex 누락된 요청 파라미터 정보를 담은 예외 객체
     * @return 누락된 파라미터 정보를 포함한 에러 응답과 400 상태 코드
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        ErrorResponse error = new ErrorResponse(
            Instant.now(),               // 타임스탬프
            HttpStatus.BAD_REQUEST.value(),    // 상태 코드 400
            "필수 요청 파라미터 '" + paramName + "'가 누락되었습니다.",  // 메시지
            ex.getMessage()                    // 상세 정보
        );
        return ResponseEntity.badRequest().body(error);
    }


    /**
     * 로그인 실패 예외를 처리합니다.
     *
     * <p>
     * 로그인 시 이메일 또는 비밀번호가 일치하지 않는 경우 발생하는 예외를 처리하여
     * 401 Unauthorized 상태 코드와 함께 오류 메시지를 반환합니다.
     * </p>
     *
     * @param ex 로그인 실패 예외
     * @return 401 Unauthorized 상태와 오류 정보를 포함한 ResponseEntity
     */
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailedException(
        LoginFailedException ex) {

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage(),
            null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

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
     * 배치 작업 실행 중 발생한 {@link NotificationBatchRunException} 예외를 처리합니다.
     *
     * @param ex 발생한 예외 객체
     * @return HTTP 500 상태 코드와 함께 에러 메시지를 포함한 {@link ErrorResponse}
     */
    @ExceptionHandler(NotificationBatchRunException.class)
    public ResponseEntity<ErrorResponse> handleNotificationBatchRunException(NotificationBatchRunException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    /**
     * 잘못된 커서 형식(ISO-8601이 아닌 문자열)이 전달된 경우 예외 처리
     *
     * @param ex 처리할 InvalidCursorFormatException
     * @return 400 Bad Request 상태와 오류 정보를 포함한 ResponseEntity
     */
    @ExceptionHandler(InvalidCursorFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCursorFormatException(InvalidCursorFormatException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "잘못된 커서 형식입니다. ISO-8601 형식을 사용하세요",
                ex.getCursor()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}