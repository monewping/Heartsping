package org.project.monewping.global.exception;

import org.project.monewping.domain.article.exception.DuplicateArticleViewsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles DuplicateArticleViewsException by returning an HTTP 409 Conflict response with the exception message.
     *
     * @param ex the DuplicateArticleViewsException that was thrown
     * @return a ResponseEntity containing the exception message and a 409 Conflict status
     */
    @ExceptionHandler(DuplicateArticleViewsException.class)
    public ResponseEntity<String> handleDuplicateArticleViewsException(DuplicateArticleViewsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

}
