package org.project.monewping.domain.article.scheduler;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.service.ArticleBackupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 뉴스 기사 백업을 일정 주기로 실행하는 스케줄러 클래스입니다.
 * 매일 자정(00:00)에 전일 데이터를 백업합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleBackupScheduler {

    private final ArticleBackupService articleBackupService;

    /**
     * 매일 00시 00분 00초에 실행됩니다.
     * 전날 뉴스 기사를 백업합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void backupYesterdayArticles() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("백업 스케줄러 실행 - 날짜 : {}", yesterday);

        try {
            articleBackupService.backupArticlesByDate(yesterday);
            log.info("백업 성공 - 날짜 : {}", yesterday);
        } catch (Exception e) {
            log.error("백업 실패 - 날짜 : {}", yesterday, e);
        }
    }

}
