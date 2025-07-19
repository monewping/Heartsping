package org.project.monewping.domain.notification.batch;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.exception.NotificationBatchRunException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job deleteOldNotificationsJob;

    /**
     * 매일 오전 5시에 실행되는 알림 삭제 배치 작업입니다.
     *
     * <p>확인된(confirmed) 알림 중, 7일이 경과된 알림을 찾아 일괄 삭제하는 작업을 수행합니다.</p>
     *
     * @throws NotificationBatchRunException 배치 작업 실행 중 오류가 발생한 경우
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void runJob() throws Exception {
        try {
            log.info("🔔 알림 삭제 배치 작업 실행 시작 - 시간: {}", Instant.now());

            JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(deleteOldNotificationsJob, params);
        } catch (Exception e) {
            log.error("배치 작업 실행 실패", e);
            throw new NotificationBatchRunException("알림 삭제 배치 실행 실패", e);
        }
    }
}