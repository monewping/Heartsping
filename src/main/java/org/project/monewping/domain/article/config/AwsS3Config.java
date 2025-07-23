package org.project.monewping.domain.article.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트를 구성하는 Spring 설정 클래스입니다.
 * <p>
 * AwsProperties에서 설정값을 주입받아 S3Client Bean을 생성합니다.
 * 이 Bean은 AWS S3와의 통신( 업로드 / 다운로드 등 )을 위한 핵심 컴포넌트입니다.
 */
@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsS3Config {

    /**
     * S3Client Bean을 생성합니다.
     * <p>
     * 이 Bean은 이후 서비스나 저장소 구현체에서 주입받아 사용할 수 있습니다.
     *
     * @param awsProperties {@link AwsProperties} 에 정의된 AWS 설정값
     * @return {@link S3Client} AWS S3와의 통신에 사용되는 클라이언트 인스턴스
     */
    @Bean
    public S3Client s3Client(AwsProperties awsProperties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
            awsProperties.accessKey(),
            awsProperties.secretKey()
        );

        return S3Client.builder()
            .region(Region.of(awsProperties.region()))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }
}
