package org.project.monewping.domain.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.notification.batch.NotificationBatchController;
import org.project.monewping.domain.notification.exception.NotificationBatchRunException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationBatchController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("Notification Batch Controller 슬라이스 테스트")
public class NotificationBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobLauncher jobLauncher;

    @MockitoBean(name = "deleteOldNotificationsJob")
    private Job deleteOldNotificationsJob;

    @Test
    @DisplayName("배치 실행 성공 시 JobLauncher 실행 후 200 OK 반환")
    void runDeleteJobManually_success() throws Exception {
        // given
        JobExecution execution = mock(JobExecution.class);

        given(jobLauncher.run(eq(deleteOldNotificationsJob), any(JobParameters.class)))
            .willReturn(execution);

        // when & then
        mockMvc.perform(post("/api/notifications/batch/delete-notifications")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(jobLauncher).run(eq(deleteOldNotificationsJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("배치 실행 중 예외 발생 시 500 에러 반환")
    void runDeleteJobManually_failure() throws Exception {
        // given
        given(jobLauncher.run(eq(deleteOldNotificationsJob), any(JobParameters.class)))
            .willThrow(new RuntimeException("DataBase 에러"));

        // when & then
        mockMvc.perform(post("/api/notifications/batch/delete-notifications")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(result ->
                assertThat(result.getResolvedException())
                    .isInstanceOf(NotificationBatchRunException.class)
            )
            .andExpect(result ->
                assertThat(Objects.requireNonNull(result.getResolvedException()).getMessage())
                    .contains("배치 실행 중 오류 발생")
            );
    }
}