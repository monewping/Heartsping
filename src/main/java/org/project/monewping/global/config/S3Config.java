package org.project.monewping.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트를 구성하는 설정 클래스
 * {@link S3Client} Bean은 모든 S3 연동 기능(로그, 백업 등)에서 공통 사용
 */
@Configuration
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class S3Config {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    /**
     * AWS S3Client Bean 생성
     * 이 클라이언트는 StaticCredentialsProvider를 이용하여 accessKey/secretKey 기반 인증을 사용
     * @return 구성된 {@link S3Client} 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        // AWS 인증 정보 구성
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        // S3Client -> 리전 및 인증 설정과 함께 생성
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
