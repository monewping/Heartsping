package org.project.monewping.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.global.service.LogUploadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 로그 파일을 정기적으로 S3에 업로드하는 스케줄러입니다.
 *
 * <p>이 스케줄러는 매일 새벽 2시에 전날 생성된 로그 파일을 S3에 업로드합니다.</p>
 * <ul>
 *     <li>업로드 실패 시에도 애플리케이션은 중단되지 않습니다.</li>
 *     <li>실패/성공 여부는 로그로만 남깁니다.</li>
 * </ul>
 *
 * @author monewping
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
public class LogUploadScheduler {

    private static final String SCHEDULER_NAME = "[LogUploadService] ";

    private final LogUploadService logUploadService;

    /**
     * 매일 새벽 2시에 전날 로그 파일을 S3에 업로드합니다.
     * <p>실패해도 애플리케이션은 중단되지 않습니다.</p>
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void uploadYesterdayLogs() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info(SCHEDULER_NAME + "로그 파일 S3 업로드 스케줄러 실행: 날짜={}", yesterday);

        try {
            logUploadService.uploadLogFile(yesterday);
        } catch (Exception e) {
            log.error(SCHEDULER_NAME + "로그 업로드 스케줄러 실행 중 예외 발생: 날짜={}", yesterday, e);
        }
    }
}
