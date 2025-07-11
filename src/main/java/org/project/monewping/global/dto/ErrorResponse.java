package org.project.monewping.global.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    String details
) {

    public static ErrorResponse of(HttpStatus status, String message, String details) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .message(message)
            .details(details)
            .build();
    }
}