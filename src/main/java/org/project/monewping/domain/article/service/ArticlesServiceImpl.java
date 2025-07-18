package org.project.monewping.domain.article.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.exception.ArticleNotFoundException;
import org.project.monewping.domain.article.exception.DuplicateArticleException;
import org.project.monewping.domain.article.exception.InterestNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticleViewsRepository;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ArticlesServiceImpl implements ArticlesService {

    private final ArticleViewsRepository articleViewsRepository;
    private final ArticlesRepository articlesRepository;
    private final InterestRepository interestRepository;
    private final ArticlesMapper articlesMapper;

    /**
     * 단일 뉴스 기사를 저장합니다.
     * 중복된 originalLink가 존재할 경우 {@link DuplicateArticleException} 예외가 발생합니다.
     *
     * @param request 저장할 뉴스 기사 요청 DTO
     * @throws DuplicateArticleException 원본 링크가 이미 존재하는 경우
     * @throws InterestNotFoundException 관심사를 찾을 수 없는 경우
     * @throws IllegalArgumentException originalLink가 비어있을 경우
     */
    @Override
    public void save(ArticleSaveRequest request) {
        log.info("뉴스 기사 저장 시도 = originalLink : {}, interestId : {}", request.originalLink(), request.interestId());

        validateOriginalLink(request.originalLink());

        if (articlesRepository.existsByOriginalLink(request.originalLink())) {
            log.warn("중복된 뉴스 기사 발견 = originalLink : {}", request.originalLink());
            throw new DuplicateArticleException(request.originalLink());
        }

        Interest interest = findInterestOrThrow(request.interestId());

        Articles article = articlesMapper.toEntity(request, interest);
        articlesRepository.save(article);

        log.info("뉴스 기사 저장 완료 = originalLink : {}", request.originalLink());
    }

    /**
     * 여러 뉴스 기사 요청을 받아 중복된 originalLink를 제외하고 저장합니다.
     *
     * @param requests 뉴스 기사 요청 리스트
     * @throws InterestNotFoundException 관심사를 찾을 수 없는 경우
     */
    @Override
    public void saveAll(List<ArticleSaveRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.info("뉴스 기사 저장 요청이 비어 있음. 처리 생략");
            return;
        }

        UUID interestId = requests.get(0).interestId();
        log.info("뉴스 기사 일괄 저장 시도 = 관심사 ID : {}, 총 요청 수 : {}", interestId, requests.size());

        Interest interest = findInterestOrThrow(interestId);

        List<String> incomingLinks = extractOriginalLinks(requests);

        List<String> existingLinks = findExistingOriginalLinks(incomingLinks);

        List<Articles> articlesToSave = filterAndMapNewArticles(requests, existingLinks, interest);

        articlesRepository.saveAll(articlesToSave);

        log.info("뉴스 기사 저장 완료 = 저장된 기사 수 : {}, 중복 제외된 기사 수 : {}",
            articlesToSave.size(), requests.size() - articlesToSave.size());
    }

    /**
     * 뉴스 기사 목록을 검색 조건에 맞게 커서 기반 페이지네이션으로 조회합니다.
     *
     * @param request 검색 조건 및 페이지네이션 정보
     * @return 커서 페이지 응답 DTO
     */
    @Override
    public CursorPageResponse<ArticleDto> findArticles(ArticleSearchRequest request) {
        log.info("뉴스 기사 목록 조회 요청 = 검색어 : {}, 관심사 ID : {}, 출처 : {}, 정렬 : {} {}, 커서: {}, after : {}",
            request.keyword(), request.interestId(), request.sourceIn(), request.orderBy(), request.direction(),
            request.cursor(), request.after());

        List<Articles> entities = articlesRepository.searchArticles(request);
        boolean hasNext = entities.size() > request.limit();

        List<Articles> page = hasNext ? entities.subList(0, request.limit()) : entities;

        // 1) 사용자 ID로 조회한 기사들 ID 목록 획득
        List<UUID> articleIds = page.stream()
            .map(Articles::getId)
            .toList();

        List<UUID> viewedArticleIds = articleViewsRepository.findAllByViewedByAndArticleIdIn(request.requestUserId(), articleIds)
            .stream()
            .map(av -> av.getArticle().getId())
            .toList();

        // 2) DTO 변환 시 viewedByMe 세팅
        List<ArticleDto> dtoList = page.stream()
            .map(article -> {
                boolean viewedByMe = viewedArticleIds.contains(article.getId());
                return articlesMapper.toDto(article).withViewedByMe(viewedByMe);
            })
            .toList();

        String nextCursor = null;
        if (hasNext) {
            Articles lastArticle = page.get(page.size() - 1);
            nextCursor = lastArticle.getId().toString();
        }

        long totalCount = articlesRepository.countArticles(request);

        log.info("뉴스 기사 목록 조회 완료 = 결과 수 : {}, 총 개수 : {}, 다음 커서 : {}",
            dtoList.size(), totalCount, nextCursor);

        return new CursorPageResponse<>(
            dtoList,
            null,
            nextCursor,
            dtoList.size(),
            totalCount,
            hasNext
        );
    }

    /**
     * 논리 삭제를 수행합니다.
     * 기사가 존재하지 않으면 예외가 발생합니다.
     *
     * @param articleId 삭제할 기사 ID
     */
    @Transactional
    public void softDelete(UUID articleId) {
        Articles article = articlesRepository.findByIdAndDeletedFalse(articleId)
            .orElseThrow(() -> {
                log.warn("뉴스 기사 논리 삭제 시도했으나, 기사 없음. articleId = {}", articleId);
                return new ArticleNotFoundException(articleId);
            });

        article.softDeleteWithMasking();
        log.info("뉴스 기사 논리 삭제 완료. articleId = {}", articleId);
    }

    /**
     * 물리 삭제를 수행합니다.
     * 연관된 ArticleViews도 함께 삭제됩니다.
     * 기사가 존재하지 않으면 예외가 발생합니다.
     *
     * @param articleId 삭제할 기사 ID
     */
    @Transactional
    public void hardDelete(UUID articleId) {
        Articles article = articlesRepository.findById(articleId)
            .orElseThrow(() -> {
                log.warn("뉴스 기사 물리 삭제 시도했으나, 기사 없음. articleId = {}", articleId);
                return new ArticleNotFoundException(articleId);
            });

        articlesRepository.delete(article);
        log.info("뉴스 기사 물리 삭제 완료. articleId = {}", articleId);
    }

    /* 내부 헬퍼 메서드로 중복 코드 제거 */

    /**
     * originalLink가 null이거나 빈 문자열인지 검사합니다.
     *
     * @param originalLink 검사할 뉴스 기사 원본 링크
     * @throws IllegalArgumentException originalLink가 null이거나 빈 문자열인 경우 발생
     */
    private void validateOriginalLink(String originalLink) {
        if (originalLink == null || originalLink.isBlank()) {
            throw new IllegalArgumentException("originalLink는 필수입니다");
        }
    }

    /**
     * 관심사 ID를 기반으로 {@link Interest} 엔티티를 조회합니다.
     *
     * @param interestId 관심사 UUID
     * @return 조회된 Interest 엔티티
     * @throws InterestNotFoundException 해당 관심사를 찾지 못했을 경우 발생
     */
    private Interest findInterestOrThrow(UUID interestId) {
        return interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));
    }

    /**
     * 뉴스 기사 요청 리스트에서 originalLink만 추출합니다.
     *
     * @param requests 뉴스 기사 저장 요청 리스트
     * @return originalLink 리스트
     */
    private List<String> extractOriginalLinks(List<ArticleSaveRequest> requests) {
        return requests.stream()
            .map(ArticleSaveRequest::originalLink)
            .toList();
    }

    /**
     * 데이터베이스에서 이미 존재하는 originalLink 목록을 조회합니다.
     *
     * @param links 비교할 originalLink 리스트
     * @return 기존에 존재하는 originalLink 리스트
     */
    private List<String> findExistingOriginalLinks(List<String> links) {
        return articlesRepository.findAllByOriginalLinkIn(links).stream()
            .map(Articles::getOriginalLink)
            .toList();
    }

    /**
     * 기존에 존재하는 originalLink를 제외하고 신규 뉴스 기사 엔티티 리스트로 변환합니다.
     *
     * @param requests 원본 뉴스 기사 요청 리스트
     * @param existingLinks 이미 존재하는 originalLink 리스트
     * @param interest 연관된 관심사 엔티티
     * @return 저장 대상 뉴스 기사 엔티티 리스트
     */
    private List<Articles> filterAndMapNewArticles(List<ArticleSaveRequest> requests, List<String> existingLinks, Interest interest) {
        return requests.stream()
            .filter(req -> !existingLinks.contains(req.originalLink()))
            .map(req -> articlesMapper.toEntity(req, interest))
            .toList();
    }

    public List<String> getAllSources() {
        List<String> sources = articlesRepository.findDistinctSources();
        // 중복 제거
        return sources.stream().distinct().collect(Collectors.toList());
    }

}
