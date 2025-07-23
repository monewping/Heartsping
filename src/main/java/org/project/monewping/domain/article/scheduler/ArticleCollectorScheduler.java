package org.project.monewping.domain.article.scheduler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.fetcher.ArticleFetcher;
import org.project.monewping.domain.article.service.ArticlesService;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.repository.KeywordRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@code ArticleCollectorScheduler}는 등록된 관심사와 키워드를 기반으로
 * 뉴스 기사를 주기적으로 수집하고 저장하는 스케줄러입니다.
 *
 * <p>각 {@link ArticleFetcher}를 통해 키워드별 기사 데이터를 수집하고,
 * {@link ArticlesService}를 통해 중복을 제거한 후 DB에 저장합니다.
 * 저장된 결과는 관심사별로 로그로 출력됩니다.</p>
 *
 * <p>이 클래스는 Spring의 {@code @Scheduled} 기능을 이용하여
 * 매 시간마다 실행되도록 설정되어 있습니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCollectorScheduler {

    private final InterestRepository interestRepository;
    private final List<ArticleFetcher> articleFetchers;
    private final ArticlesService articlesService;
    private final KeywordRepository keywordRepository;

    /**
     * 등록된 모든 관심사에 대해 뉴스 기사 수집을 수행하는 스케줄러 메서드입니다.
     *
     * <p>관심사별로 키워드를 조회한 뒤, 모든 {@link ArticleFetcher}를 사용해
     * 기사 데이터를 수집하고 {@link ArticlesService#saveAll(List)}를 통해 저장합니다.</p>
     *
     * <p>수집 및 저장 결과는 전체 수와 관심사별 저장 수로 로그에 출력됩니다.</p>
     *
     * <p><strong>스케줄 주기</strong>: 매 정시마다 ( 매 시간 0분 )</p>
     */
    @Scheduled(cron = "0 0 * * * *")
    public void collectArticlesByInterest() {
        log.info("[ 뉴스 기사 수집 배치 시작 ]");

        List<Interest> interests = interestRepository.findAll();
        int totalSaved = 0;

        // 관심사별 저장 기사 수 집계용 Map
        Map<String, Integer> savedCountByInterest = new LinkedHashMap<>();

        // 모든 관심사에 대해 기사 수집 시도
        for (Interest interest : interests) {
            int saved = collectForInterest(interest);
            totalSaved += saved;
            savedCountByInterest.put(interest.getName(), saved);
        }

        log.info("[ 수집 완료 ] 전체 저장된 기사 수 : {}", totalSaved);

        if (!savedCountByInterest.isEmpty()) {
            log.info("[ 관심사별 저장 현황 ]");
            savedCountByInterest.forEach((name, count) ->
                log.info(" - {} : {}개", name, count));
        }
    }

    /**
     * 주어진 관심사에 대해 키워드별 기사 수집 및 저장을 수행합니다.
     *
     * <p>각 키워드에 대해 모든 {@link ArticleFetcher}를 순회하며 기사 수집을 시도하고,
     * {@link ArticlesService#saveAll(List)}를 통해 저장된 기사 수를 집계합니다.</p>
     *
     * @param interest 수집 대상 관심사
     * @return 해당 관심사에 대해 최종적으로 저장된 기사 수
     */
    private int collectForInterest(Interest interest) {
        UUID interestId = interest.getId();
        // 관심사에 연결된 키워드 리스트 조회
        List<String> keywords = keywordRepository.findNamesByInterestId(interestId);

        int savedCount = 0;

        log.info("관심사 '{}' ({}) 수집 시작 - 키워드 개수 : {}", interest.getName(), interestId, keywords.size());

        for (String keyword : keywords) {
            for (ArticleFetcher fetcher : articleFetchers) {
                try {
                    // 수집기별 기사 수집
                    List<String> singleKeywordList = List.of(keyword);
                    List<ArticleSaveRequest> articles = fetcher.fetch(interestId, singleKeywordList);

                    if (articles.isEmpty()) {
                        log.debug("수집 결과 없음 - fetcher : {}, keyword : {}", fetcher.getClass().getSimpleName(), keyword);
                        continue;
                    }

                    // 기사 저장
                    int saveNum = articlesService.saveAll(articles);
                    savedCount += saveNum;

                    log.info("'{}' - 키워드 '{}' - {}개 기사 저장 (Fetcher: {})",
                        interest.getName(), keyword, saveNum, fetcher.getClass().getSimpleName());

                } catch (Exception e) {
                    // 수집 실패 시 에러 로그 남기고 다음 fetcher로 진행
                    log.warn("'{}' - fetcher '{}' 키워드 '{}' 에러: {}",
                        interest.getName(), fetcher.getClass().getSimpleName(), keyword, e.getMessage(), e);
                }
            }
        }

        log.info("관심사 '{}' 수집 완료 - 저장된 기사 수: {}", interest.getName(), savedCount);
        return savedCount;
    }

}
