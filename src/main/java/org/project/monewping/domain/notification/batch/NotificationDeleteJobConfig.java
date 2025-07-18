package org.project.monewping.domain.notification.batch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.exception.NotificationBatchRunException;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationRepository notificationRepository;

    @Bean(name = "배치 작업 정의")
    public Job deleteOldNotificationsJob() {
        return new JobBuilder("deleteOldNotificationsJob", jobRepository)
            .start(deleteOldNotificationsStep())
            .build();
    }

    @Bean(name = "하나의 단위 작업 정의 (알림 조회 -> 삭제)")
    public Step deleteOldNotificationsStep() {
        return new StepBuilder("deleteOldNotificationsStep", jobRepository)
            .<Notification, Notification>chunk(100, transactionManager)
            .processor(loggingProcessor())
            .reader(notificationReader())
            .writer(notificationWriter())
            .build();
    }

    @Bean(name = "삭제할 알림 조회")
    public ItemReader<Notification> notificationReader() {
        Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        return new RepositoryItemReaderBuilder<Notification>()
            .name("notificationReader")
            .repository(notificationRepository)
            .methodName("findAllByConfirmedIsTrueAndUpdatedAtBefore")
            .arguments(List.of(oneWeekAgo))
            .pageSize(100)
            .sorts(Map.of("id", Sort.Direction.ASC))
            .build();
    }

    @Bean(name = "로그 출력")
    public ItemProcessor<Notification, Notification> loggingProcessor() {
        return item -> {
            log.info("삭제 알림의 ID: {}", item.getId());
            return item;
        };
    }

    @Bean(name = "알림 삭제")
    public ItemWriter<Notification> notificationWriter() {
        return items -> {
            try {
                List<Notification> notificationsForDelete = items.getItems().stream()
                    .map(n -> (Notification) n)
                    .toList();
                notificationRepository.deleteAllInBatch(notificationsForDelete);
            } catch (Exception e) {
                log.error("알림 삭제 중 예외 발생", e);
                throw new NotificationBatchRunException("알림 삭제 실패", e);
            }
        };
    }
}