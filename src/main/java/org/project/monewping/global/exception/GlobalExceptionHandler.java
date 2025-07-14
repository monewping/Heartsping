package org.project.monewping.global.exception;

import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.global.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 관심사 도메인 커스텀 예외만 추가
    @ExceptionHandler(DuplicateInterestNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateInterestNameException(DuplicateInterestNameException e) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT,
                "DUPLICATE_INTEREST_NAME",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(SimilarInterestNameException.class)
    public ResponseEntity<ErrorResponse> handleSimilarInterestNameException(SimilarInterestNameException e) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT,
                "SIMILAR_INTEREST_NAME",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InterestCreationException.class)
    public ResponseEntity<ErrorResponse> handleInterestCreationException(InterestCreationException e) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTEREST_CREATION_ERROR",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        String details = "필드 오류: " + fieldErrors.toString();
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 