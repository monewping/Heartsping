package org.project.monewping.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AWS S3 관련 설정을 구성하는 설정 레코드
 * <p>
 * 이 클래스는 다음과 같은 하위 설정을 포함합니다:
 * <ul>
 *     <li>{@code backup} - 백업 관련 S3 설정</li>
 *     <li>{@code logs} - 로그 업로드 관련 S3 설정</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(
    Backup backup,
    Logs logs
) {
    /**
     * S3 백업 기능에 대한 설정입니다.
     *
     * <p>예시 YAML 경로: {@code aws.s3.backup}</p>
     *
     * @param enabled        백업 기능 활성화 여부
     * @param bucketName     백업 대상 S3 버킷 이름 (예: {@code monewping-articles-storage})
     * @param baseDirectory  백업 파일 저장 경로 접두사 (예: {@code backup/articles})
     */
    public record Backup(
        boolean enabled,
        String bucketName,
        String baseDirectory
    ) {}

    /**
     * S3 로그 업로드 기능에 대한 설정입니다.
     *
     * <p>예시 YAML 경로: {@code aws.s3.logs}</p>
     *
     * @param enabled     로그 업로드 기능 활성화 여부
     * @param bucketName  로그 저장용 S3 버킷 이름 (예: {@code monewping-logs-storage})
     * @param prefix      로그 저장 위치 접두사 (예: {@code application-logs/})
     */
    public record Logs(
        boolean enabled,
        String bucketName,
        String prefix
    ) {}
}
