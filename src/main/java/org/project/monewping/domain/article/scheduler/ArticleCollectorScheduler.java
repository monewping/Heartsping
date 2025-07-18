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
     * 매 시간 정각에 실행되는 뉴스 기사 수집 배치 작업입니다.
     * <p>
     * 모든 관심사를 조회하여 각 관심사의 키워드로 외부 API를 통해 기사를 수집하고,
     * 중복을 제외한 기사를 저장합니다.
     * <p>
     * - 관심사 이름을 키워드로 사용하여 수집
     * - 수집된 기사 리스트를 {@link ArticlesService#saveAll} 메서드를 통해 저장
     * - 수집 시작 및 종료 시점, 관심사별 수집 건수에 대한 로그를 기록
     *
     * @throws RuntimeException 수집 중 예외 발생 시 로그로 기록하며 배치 수행 중단하지 않음
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
                try {
                    List<ArticleSaveRequest> fetchedArticles = fetcher.fetch(keyword);

                    // 방어코드: null일 경우 빈 리스트로 초기화
                    if (fetchedArticles == null) {
                        fetchedArticles = List.of();
                    }

                    List<ArticleSaveRequest> articlesToSave = fetchedArticles.stream()
                        .map(article -> new ArticleSaveRequest(
                            interestId,
                            article.source(),
                            article.originalLink(),
                            article.title(),
                            article.summary(),
                            article.publishedAt()
                        ))
                        .toList();

                    if (articlesToSave.isEmpty()) {
                        log.info("관심사 키워드 '{}'에 대해 0개 기사 수집 완료", keyword);
                        continue; // 빈 리스트면 저장 호출 안 함
                    }

                    articlesService.saveAll(articlesToSave);
                    totalCount += articlesToSave.size();

                    log.info("관심사 키워드 '{}'에 대해 {}개 기사 수집 완료", keyword, articlesToSave.size());
                } catch (Exception e) {
                    log.error("기사 수집 중 예외 발생 = 키워드 : '{}', 에러 : {}", keyword, e.getMessage(), e);
                }
            }
        }

        log.info("[ 뉴스 기사 수집 배치 종료 ] 총 수집 시도 기사 수 : {}", totalCount);
    }

}
