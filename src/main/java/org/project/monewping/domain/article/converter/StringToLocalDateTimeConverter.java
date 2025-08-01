package org.project.monewping.domain.article.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            // 우선 LocalDateTime 으로 파싱 시도
            return LocalDateTime.parse(source);
        } catch (DateTimeParseException e) {
            try {
                // 실패하면 LocalDate로 파싱 후 자정으로 변환
                LocalDate date = LocalDate.parse(source);
                return date.atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid date format : " + source, ex);
            }
        }
    }
}
