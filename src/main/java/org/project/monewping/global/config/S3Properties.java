package org.project.monewping.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 관련 공통 설정 레코드
 *
 * @param bucket                백업 기본 버킷 (ex. monewping-articles-storage)
 * @param baseDirectory         백업 기본 경로 접두사 (ex. backup/articles)
 * @param enabled               S3 백업 기능 활성화 여부
 * @param logs                  로그 설정 (내부 record)
 */
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
    String bucket,
    String baseDirectory,
    boolean enabled,
    Logs logs
) {
    /**
     * S3 로그 전용 설정입니다.
     *
     * @param bucketName 로그 저장용 S3 버킷 이름
     * @param prefix     로그 저장 위치 (ex. application-logs/)
     */
    public record Logs(
       String bucketName,
       String prefix
    ) {}
}
