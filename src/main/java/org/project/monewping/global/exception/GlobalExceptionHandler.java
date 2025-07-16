package org.project.monewping.global.exception;

import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST, "유효성 검사 실패", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), null);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "잘못된 JSON 형식입니다.";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "JSON 파싱 오류: 올바른 JSON 형식으로 요청해주세요.";
        }
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.BAD_REQUEST, message, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}