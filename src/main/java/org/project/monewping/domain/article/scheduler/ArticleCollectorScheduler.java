package org.project.monewping.domain.article.scheduler;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.domain.article.fetcher.ArticleFetcher;
import org.project.monewping.domain.article.repository.InterestRepository;
import org.project.monewping.domain.article.service.ArticlesService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCollectorScheduler {

    private final InterestRepository interestRepository;
    private final List<ArticleFetcher> articleFetchers;
    private final ArticlesService articlesService;

    /**
     * Periodically collects news articles for all user interests and saves them in bulk.
     * <p>
     * Runs at the start of every hour, fetching articles from external sources for each interest keyword,
     * transforming them into save requests, and persisting them via the articles service.
     * Logs the start and end of the batch process, as well as the number of articles collected per interest.
     * Any exceptions during collection are logged but do not interrupt the batch execution.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void collectArticlesByInterest() {
        log.info("[ 뉴스 기사 수집 배치 시작 ]");

        List<Interest> interests = interestRepository.findAll();
        int totalCount = 0;

        for (Interest interest : interests) {
            String keyword = interest.getName();
            UUID interestId = interest.getId();

            for (ArticleFetcher fetcher : articleFetchers) {
                List<ArticleSaveRequest> fetchedArticles = fetcher.fetch(keyword).stream()
                    .map(article -> new ArticleSaveRequest(
                        interestId,
                        article.source(),
                        article.originalLink(),
                        article.title(),
                        article.summary(),
                        article.publishedAt()
                    ))
                    .toList();

                articlesService.saveAll(fetchedArticles);
                totalCount += fetchedArticles.size();

                log.info("관심사 키워드 '{}'에 대해 {}개 기사 수집 완료", keyword,fetchedArticles.size());
            }

        }

        log.info("[ 뉴스 기사 수집 배치 종료 ] 총 수집 시도 기사 수 : {}", totalCount);

    }

}
