package org.project.monewping.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.global.config.S3Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 로그 파일을 AWS S3에 업로드하는 서비스 클래스입니다.
 * 
 * <p>이 서비스는 다음과 같은 기능을 제공합니다:</p>
 * <ul>
 *     <li>지정된 날짜의 로그 파일을 로컬에서 찾기</li>
 *     <li>로그 파일을 S3 버킷에 업로드</li>
 *     <li>업로드 결과 로깅 및 에러 처리</li>
 * </ul>
 * 
 * <p>주의사항:</p>
 * <ul>
 *     <li>S3 업로드 실패 시에도 애플리케이션이 중단되지 않습니다</li>
 *     <li>모든 예외는 로그로만 기록되고 정상 종료됩니다</li>
 *     <li>파일이 존재하지 않으면 업로드를 건너뜁니다</li>
 * </ul>
 * 
 * @author monewping
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "aws.s3.logs.enabled", havingValue = "true")
public class LogUploadService {

    private static final String SERVICE_NAME = "[LogUploadService] ";

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Value("${logging.file.path: ./logs}")
    private String logPath;

    @Value("${logging.file.name:monewping}")
    private String logFileName;

    /**
     * 지정된 날짜의 모든 로그 파일을 S3에 업로드합니다.
     * 
     * <p>업로드 과정:</p>
     * <ol>
     *     <li>지정된 날짜의 모든 로그 파일 경로를 생성합니다</li>
     *     <li>각 파일 존재 여부를 확인합니다</li>
     *     <li>존재하는 파일들을 S3에 업로드합니다</li>
     *     <li>업로드 결과를 로그로 기록합니다</li>
     * </ol>
     * 
     * <p>업로드되는 로그 파일들:</p>
     * <ul>
     *     <li>일반 로그: monewping-{date}.log</li>
     *     <li>에러 로그: monewping-error-{date}.log</li>
     *     <li>SQL 로그: monewping-sql-{date}.log</li>
     * </ul>
     * 
     * <p>예외 처리:</p>
     * <ul>
     *     <li>파일이 존재하지 않으면 해당 파일만 건너뜁니다</li>
     *     <li>IOException 발생 시 에러 로그를 남기고 계속 진행합니다</li>
     *     <li>기타 예외 발생 시 에러 로그를 남기고 계속 진행합니다</li>
     *     <li>모든 경우에 애플리케이션은 정상적으로 계속 실행됩니다</li>
     * </ul>
     * 
     * @param date 업로드할 로그 파일의 날짜
     */
    public void uploadLogFile(LocalDate date) {
        log.info(SERVICE_NAME + "로그 파일 S3 업로드 시작: 날짜={}", date);

        String bucketName = s3Properties.logs().bucketName();
        String prefix = s3Properties.logs().prefix();

        log.info(SERVICE_NAME + "S3 설정 - 버킷: {}, 접두사: {}, 로그경로: {}", bucketName, prefix, logPath);

        // 로그 디렉토리 확인
        Path logDir = Paths.get(logPath);
        log.info(SERVICE_NAME + "로그 디렉토리 존재 여부: {}", Files.exists(logDir));

        if (Files.exists(logDir)) {
            try {
                log.info(SERVICE_NAME + "로그 디렉토리 내용:");
                Files.list(logDir).forEach(file ->
                    log.info(SERVICE_NAME + "  - {}", file.getFileName()));
            } catch (IOException e) {
                log.error(SERVICE_NAME + "로그 디렉토리 읽기 실패", e);
            }
        }

        // 업로드할 로그 파일 목록
        String[] logTypes = {"", "-error", "-sql"};
        int uploadedCount = 0;

        for (String logType : logTypes) {
            try {
                Path logFile = getLogFilePath(date, logType);

                if (!logFileExists(logFile)) {
                    log.warn(SERVICE_NAME + "업로드할 로그 파일이 존재하지 않습니다: {}", logFile);
                    continue;
                }

                String s3Key = generateS3Key(date, logType, prefix);
                byte[] fileContent = Files.readAllBytes(logFile);

                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("text/plain")
                        .build(), RequestBody.fromBytes(fileContent));

                log.info(SERVICE_NAME + "로그 파일 S3 업로드 완료: type={}, bucket={}, key={}, size={} bytes",
                        logType.isEmpty() ? "general" : logType.substring(1), bucketName, s3Key, fileContent.length);
                uploadedCount++;

            } catch (IOException e) {
                log.error(SERVICE_NAME + "로그 파일 S3 업로드 실패: type={}, 날짜={}", 
                        logType.isEmpty() ? "general" : logType.substring(1), date, e);
            } catch (Exception e) {
                log.error(SERVICE_NAME + "로그 파일 S3 업로드 중 예외 발생: type={}, 날짜={}", 
                        logType.isEmpty() ? "general" : logType.substring(1), date, e);
            }
        }

        log.info(SERVICE_NAME + "로그 파일 S3 업로드 완료: 날짜={}, 업로드된 파일 수={}", date, uploadedCount);
    }

    /**
     * 지정된 날짜의 로그 파일 경로를 생성합니다.
     * 
     * @param date 로그 파일 날짜
     * @param logType 로그 타입 ("", "-error", "-sql")
     * @return 로그 파일의 전체 경로
     */
    private Path getLogFilePath(LocalDate date, String logType) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return Paths.get(logPath, logFileName + logType + "-" + dateStr + ".log");
    }

    /**
     * S3에 업로드할 파일의 키(경로)를 생성합니다.
     * 
     * @param date 로그 파일 날짜
     * @param logType 로그 타입 ("", "-error", "-sql")
     * @param prefix S3 버킷 내 저장 경로 접두사
     * @return S3 객체 키 (예: application-logs/2025-07-22/monewping-2025-07-22.log)
     */
    private String generateS3Key(LocalDate date, String logType, String prefix) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return String.format("%s/%s/%s-%s.log", prefix, dateStr, logFileName + logType, dateStr);
    }

    /**
     * 로그 파일의 존재 여부를 확인합니다.
     * 
     * @param logFile 확인할 로그 파일 경로
     * @return 파일이 존재하면 true, 존재하지 않으면 false
     */
    private boolean logFileExists(Path logFile) {
        return Files.exists(logFile);
    }
}
