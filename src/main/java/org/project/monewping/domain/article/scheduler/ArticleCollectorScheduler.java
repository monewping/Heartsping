package org.project.monewping.domain.article.scheduler;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.fetcher.ArticleFetcher;
import org.project.monewping.domain.article.service.ArticlesService;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;
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
     * 매 시간마다 실행되는 뉴스 기사 수집 스케줄러입니다.
     *
     * 전체 관심사를 조회하여 각 관심사 이름을 키워드로 사용해
     * 모든 수집기를 통해 기사 데이터를 수집하고, ArticlesService를 통해 저장합니다.
     */
    @Scheduled(cron = "0 */3 * * * *")
    public void collectArticlesByInterest() {
        log.info("[🗞️ 뉴스 기사 수집 배치 시작]");

        List<Interest> interests = interestRepository.findAll();
        int totalSaved = 0;

        // 모든 관심사에 대해 기사 수집 시도
        for (Interest interest : interests) {
            int saved = collectForInterest(interest);
            totalSaved += saved;
        }

        log.info("[✅ 수집 완료] 전체 저장된 기사 수: {}", totalSaved);
    }

    /**
     * 주어진 관심사에 대해 각 fetcher를 호출하여 기사를 수집하고 저장합니다.
     *
     * @param interest 수집 대상 관심사
     * @return 저장된 기사 수
     */
    private int collectForInterest(Interest interest) {
        UUID interestId = interest.getId();
        String keyword = interest.getName();
        int savedCount = 0;

        log.info("▶ 관심사 '{}' ({}) 수집 시작", keyword, interestId);

        for (ArticleFetcher fetcher : articleFetchers) {
            try {
                // 수집기별 기사 수집
                List<ArticleSaveRequest> articles = fetcher.fetch(interestId, keyword);

                if (articles.isEmpty()) {
                    log.debug("⛔ 수집 결과 없음 - fetcher: {}", fetcher.getClass().getSimpleName());
                    continue;
                }

                // 기사 저장
                articlesService.saveAll(articles);
                savedCount += articles.size();

                log.info("✔️ '{}' - {}개 기사 저장 (Fetcher: {})", keyword, articles.size(), fetcher.getClass().getSimpleName());

            } catch (Exception e) {
                // 수집 실패 시 에러 로그 남기고 다음 fetcher로 진행
                log.warn("❌ '{}' - fetcher '{}' 에러: {}", keyword, fetcher.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        log.info("■ 관심사 '{}' 수집 완료 - 저장된 기사 수: {}", keyword, savedCount);
        return savedCount;
    }

}
