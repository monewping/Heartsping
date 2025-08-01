package org.project.monewping.domain.article.config;

import org.project.monewping.domain.article.converter.StringToLocalDateTimeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final StringToLocalDateTimeConverter converter;

    public WebConfig(StringToLocalDateTimeConverter converter) {
        this.converter = converter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(converter);
    }

}
