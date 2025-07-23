package org.project.monewping.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3Config {

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Value("${aws.s3.logs.bucket-name:monewping-logs-storage}")
    private String bucketName;

    @Value("${aws.s3.logs.prefix:application-logs}")
    private String prefix;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
