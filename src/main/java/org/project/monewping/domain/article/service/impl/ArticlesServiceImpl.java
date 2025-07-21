package org.project.monewping.domain.article.service.impl;

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
import org.project.monewping.domain.article.exception.InterestNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticleViewsRepository;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.service.ArticlesService;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 뉴스 기사 관련 서비스 구현체
 */
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
     * 여러 뉴스 기사 요청을 받아 중복된 originalLink를 제외하고 저장합니다.
     *
     * <p>동일 originalLink가 이미 DB에 존재하면 해당 기사는 저장하지 않습니다.
     * 또한 originalLink가 null이거나 공백이면 무시됩니다.</p>
     *
     * @param requests 뉴스 기사 저장 요청 리스트
     * @throws org.project.monewping.domain.article.exception.InterestNotFoundException 관심사를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void saveAll(List<ArticleSaveRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            log.info("[saveAll] 저장 요청이 비어있음");
            return;
        }

        // 관심사 ID는 모든 요청이 동일하다고 가정
        UUID interestId = requests.get(0).interestId();
        Interest interest = findInterestOrThrow(interestId);

        // 유효한 요청 필터링 ( originalLink null / 빈 값 제거 )
        List<ArticleSaveRequest> validRequests = requests.stream()
            .filter(req -> req.originalLink() != null && !req.originalLink().isBlank())
            .toList();

        if (validRequests.isEmpty()) {
            log.info("[saveAll] 유효한 저장 대상 없음");
            return;
        }

        // 요청 원본링크 리스트
        List<String> originalLinks = validRequests.stream()
            .map(ArticleSaveRequest::originalLink)
            .toList();

        // DB에 이미 존재하는 링크 조회
        List<String> existingLinks = articlesRepository.findAllByOriginalLinkIn(originalLinks).stream()
            .map(Articles::getOriginalLink)
            .toList();

        // 신규 요청만 필터링 후 엔티티 변환
        List<Articles> articlesToSave = validRequests.stream()
            .filter(req -> !existingLinks.contains(req.originalLink()))
            .map(req -> articlesMapper.safeToEntity(req, interest))
            .toList();

        if (articlesToSave.isEmpty()) {
            log.info("[saveAll] 저장할 신규 뉴스 기사 없음");
            return;
        }

        // 저장
        articlesRepository.saveAll(articlesToSave);

        log.info("[saveAll] 뉴스 기사 저장 완료 - count: {}", articlesToSave.size());
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
     * 뉴스 기사를 논리적으로 삭제합니다.
     * 해당 기사는 실제로 삭제되지 않고, 마스킹된 채 DB에 유지됩니다.
     *
     * @param articleId 삭제할 기사 UUID
     * @throws ArticleNotFoundException 해당 ID의 기사가 존재하지 않는 경우
     */
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
     * 뉴스 기사를 DB에서 완전히 삭제합니다.
     * 연관된 조회 기록도 함께 삭제되어야 합니다.
     *
     * @param articleId 삭제할 기사 UUID
     * @throws ArticleNotFoundException 해당 ID의 기사가 존재하지 않는 경우
     */
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
     * 관심사 UUID를 기반으로 Interest 엔티티를 조회합니다.
     *
     * @param interestId 관심사 ID
     * @return 조회된 Interest
     * @throws InterestNotFoundException 존재하지 않는 경우
     */
    private Interest findInterestOrThrow(UUID interestId) {
        return interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));
    }

    /**
     * 저장된 뉴스 기사들의 출처(source)를 중복 제거 후 반환합니다.
     *
     * @return 중복 없는 출처 목록
     */
    public List<String> getAllSources() {
        List<String> sources = articlesRepository.findDistinctSources();
        // 중복 제거
        return sources.stream().distinct().collect(Collectors.toList());
    }

}