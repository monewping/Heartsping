package org.project.monewping.domain.article.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.domain.article.exception.DuplicateArticleException;
import org.project.monewping.domain.article.exception.InterestNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.repository.InterestRepository;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ArticlesServiceImpl implements ArticlesService {

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
     */
    @Override
    public void save(ArticleSaveRequest request) {
        log.info("뉴스 기사 저장 시도 = originalLink : {}, interestId : {}", request.originalLink(), request.interestId());

        if (request.originalLink() == null || request.originalLink().isBlank()) {
            throw new IllegalArgumentException("originalLink는 필수입니다");
        }

        if (articlesRepository.existsByOriginalLink(request.originalLink())) {
            log.warn("중복된 뉴스 기사 발견 = originalLink : {}", request.originalLink());
            throw new DuplicateArticleException(request.originalLink());
        }

        Interest interest = interestRepository.findById(request.interestId())
            .orElseThrow(() -> new InterestNotFoundException(request.interestId()));

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

        Interest interest = interestRepository.findById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));

        // 1. 수집된 기사들의 링크 목록 추출
        List<String> incomingLinks = requests.stream()
            .map(ArticleSaveRequest::originalLink)
            .toList();

        // 2. DB에 이미 존재하는 기사 링크들 조회
        List<String> existingLinks = articlesRepository.findAllByOriginalLinkIn(incomingLinks).stream()
            .map(Articles::getOriginalLink)
            .toList();

        // 3. 중복되지 않은 요청만 필터링
        List<Articles> articlesToSave = requests.stream()
            .filter(req -> !existingLinks.contains(req.originalLink()))
            .map(req -> articlesMapper.toEntity(req, interest))
            .toList();

        // 4. 저장
        articlesRepository.saveAll(articlesToSave);

        log.info("뉴스 기사 저장 완료 = 저장된 기사 수 : {}, 중복되어 제외된 수 : {}",
            articlesToSave.size(), requests.size() - articlesToSave.size());

    }

    @Override
    public CursorPageResponse<ArticleDto> findArticles(ArticleSearchRequest request) {
        List<Articles> entities = articlesRepository.searchArticles(request);

        boolean hasNext = entities.size() > request.limit();

        // 페이징 사이즈만큼 자르기
        List<Articles> page = hasNext ? entities.subList(0, request.limit()) : entities;

        List<ArticleDto> dtoList = page.stream()
            .map(articlesMapper::toDto)
            .toList();

        // 다음 커서 계산 (마지막 아이템 기준)
        String nextCursor = null;
        if (hasNext) {
            Articles lastArticle = page.get(page.size() - 1);
            nextCursor = lastArticle.getId().toString();
        }

        long totalCount = articlesRepository.countArticles(request);

        return new CursorPageResponse<>(
            dtoList,
            null, // nextIdAfter는 Long 타입인데, UUID 커서라 null 처리
            nextCursor,
            dtoList.size(),
            totalCount,
            hasNext
        );
    }

}
