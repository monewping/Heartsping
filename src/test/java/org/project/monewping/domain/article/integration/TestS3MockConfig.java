package org.project.monewping.domain.article.integration;

import org.mockito.Mockito;
import org.project.monewping.domain.article.storage.S3ArticleBackupStorage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestS3MockConfig {

    @Bean
    public S3ArticleBackupStorage s3ArticleBackupStorage() {
        return Mockito.mock(S3ArticleBackupStorage.class);
    }
}
