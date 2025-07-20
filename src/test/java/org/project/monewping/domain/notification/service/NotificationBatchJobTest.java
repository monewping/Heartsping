package org.project.monewping.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.notification.batch.NotificationDeleteJobConfig;
import org.project.monewping.domain.notification.entity.Notification;
import org.project.monewping.domain.notification.exception.NotificationBatchRunException;
import org.project.monewping.domain.notification.repository.NotificationRepository;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Batch Job 단위 테스트")
public class NotificationBatchJobTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private NotificationDeleteJobConfig jobConfig;

    @Test
    @DisplayName("loggingProcessor는 입력된 Notification을 반환한다")
    void testLoggingProcessor() throws Exception {
        // given
        Notification notification = new Notification(
            UUID.randomUUID(),
            "테스트 알림",
            UUID.randomUUID(),
            "COMMENT"
        );

        // when
        ItemProcessor<Notification, Notification> processor = jobConfig.loggingProcessor();

        Notification result = processor.process(notification);

        // then
        assertThat(result)
            .as("배치 실행 후 반환되는 객체(result)는 notification과 동일해야 한다")
            .isEqualTo(notification);
    }

    @Test
    @DisplayName("notificationWriter는 deleteAllInBatch를 호출한다")
    void testNotificationWriter() throws Exception {
        // given
        Notification notification = new Notification(
            UUID.randomUUID(),
            "테스트 알림",
            UUID.randomUUID(),
            "COMMENT"
        );

        Chunk<Notification> items = new Chunk<>(List.of(notification));
        ItemWriter<Notification> writer = jobConfig.notificationWriter();

        // when
        writer.write(items);

        // then
        verify(notificationRepository).deleteAllInBatch(items.getItems());
    }

    @Test
    @DisplayName("notificationWriter는 삭제 중 예외 발생 시 커스텀 예외를 던진다")
    void testNotificationWriterThrowsCustomException() {
        // given
        Notification notification = new Notification(
            UUID.randomUUID(),
            "테스트 알림",
            UUID.randomUUID(),
            "COMMENT"
        );

        Chunk<Notification> items = new Chunk<>(List.of(notification));

        doThrow(new RuntimeException("DataBase Error"))
            .when(notificationRepository).deleteAllInBatch(any());

        ItemWriter<Notification> writer = jobConfig.notificationWriter();

        // when and then
        assertThatThrownBy(() -> writer.write(items))
            .isInstanceOf(NotificationBatchRunException.class)
            .hasMessageContaining("알림 삭제 실패");
    }
}