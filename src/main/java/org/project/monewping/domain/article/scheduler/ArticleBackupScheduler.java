package org.project.monewping.domain.article.scheduler;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.service.ArticleBackupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleBackupScheduler {

    private final ArticleBackupService articleBackupService;

    /**
     * 매일 자정에 어제 날짜 기준 기사 백업 수행
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void backupYesterdayArticles() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("뉴스 기사 백업 스케줄러 시작 = 백업 대상 날짜 : {}", yesterday);
        articleBackupService.backupArticlesByDate(yesterday);
    }

}
