package org.project.monewping.domain.article.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.entity.Articles;
import org.project.monewping.domain.article.entity.Interest;
import org.project.monewping.domain.article.exception.DuplicateArticleException;
import org.project.monewping.domain.article.exception.InterestNotFoundException;
import org.project.monewping.domain.article.mapper.ArticlesMapper;
import org.project.monewping.domain.article.repository.ArticlesRepository;
import org.project.monewping.domain.article.repository.InterestRepository;
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
     * Saves a single news article.
     *
     * Throws a {@link DuplicateArticleException} if an article with the same original link already exists,
     * or an {@link InterestNotFoundException} if the specified interest does not exist.
     *
     * @param request the DTO containing the news article data to save
     * @throws DuplicateArticleException if an article with the same original link already exists
     * @throws InterestNotFoundException if the specified interest is not found
     */
    @Override
    public void save(ArticleSaveRequest request) {
        log.info("뉴스 기사 저장 시도 = originalLink : {}, interestId : {}", request.originalLink(), request.interestId());

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
     * Saves multiple news articles, excluding those with original links that already exist in the database.
     *
     * If the provided list is null or empty, the method returns immediately without performing any operation.
     *
     * @param requests List of article save requests to process.
     * @throws InterestNotFoundException if the specified interest ID does not correspond to any existing interest.
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
}
