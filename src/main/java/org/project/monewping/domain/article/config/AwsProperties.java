package org.project.monewping.domain.article.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS 관련 설정 정보를 바인딩하는 구성 레코드입니다.
 * <p>
 * application.yml 또는 application-{profile}.yml의 {@code aws} 설정을
 * 자바 객체로 매핑하여 주입 가능한 형태로 제공합니다.
 * @param accessKey AWS IAM Access Key
 * @param secretKey AWS IAM Secret Key
 * @param region    AWS 리전
 * @param s3        S3 설정 정보 ( 버킷명, 경로, 활성화 여부 등 )
 */
@ConfigurationProperties(prefix = "aws")
public record AwsProperties(
    String accessKey,
    String secretKey,
    String region,
    S3 s3
) {
    /**
     * AWS S3 관련 설정을 담는 서브 레코드입니다.
     *
     * @param bucket         사용할 S3 버킷 이름
     * @param baseDirectory  백업 데이터가 저장될 기본 경로
     * @param enabled        S3 연동 활성화 여부 ( true면 사용 )
     */
    public record S3(
        String bucket,
        String baseDirectory,
        boolean enabled
    ) {}
}