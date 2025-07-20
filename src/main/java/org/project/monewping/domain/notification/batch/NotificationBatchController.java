package org.project.monewping.domain.notification.batch;

import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.notification.exception.NotificationBatchRunException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationBatchController {

    private final JobLauncher jobLauncher;
    private final Job deleteOldNotificationsJob;

    /**
     * 수동으로 알림 삭제 배치 작업을 실행하는 API 엔드포인트입니다.
     *
     * @throws NotificationBatchRunException 배치 작업 실행 중 예외 발생 시
     */
    @PostMapping("/batch/delete-notifications")
    public void runDeleteJobManually() throws Exception {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(deleteOldNotificationsJob, jobParameters);
        } catch (Exception e) {
            throw new NotificationBatchRunException("배치 실행 중 오류 발생", e);
        }
    }
}

