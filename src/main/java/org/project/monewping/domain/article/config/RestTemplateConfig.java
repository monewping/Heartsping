package org.project.monewping.domain.article.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    /**
     * Creates and provides a RestTemplate bean for performing HTTP requests within the Spring application context.
     *
     * @return a new RestTemplate instance managed by Spring
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
