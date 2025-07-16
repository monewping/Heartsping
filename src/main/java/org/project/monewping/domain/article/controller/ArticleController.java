package org.project.monewping.domain.article.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.service.ArticleViewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 뉴스 기사 조회 기록 등록 관련 REST API 컨트롤러.
 * <p>
 * 클라이언트로부터 사용자 ID와 기사 ID를 받아
 * 조회 기록 등록 요청을 처리한다.
 * </p>
 */

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleViewsService articleViewsService;

    /****
     * Registers a view record for a specific news article by a user.
     *
     * @param articleId the ID of the news article to register a view for
     * @param viewedBy the ID of the user viewing the article, provided in the "Monew-Request-User-ID" request header
     * @return the registered article view record as an ArticleViewDto
     */
    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> registerArticleView(
        @PathVariable UUID articleId,
        @RequestHeader("Monew-Request-User-ID") UUID viewedBy
    ) {
        log.info("기사 조회 등록 요청 수신 : userId = {}, articleId = {}", viewedBy, articleId);

        // Service로 사용자 ID, 기사 ID 전달
        ArticleViewDto responseDto = articleViewsService.registerView(viewedBy, articleId);

        log.info("기사 조회 등록 완료 : id = {}, userId = {}, articleId = {}, createdAt = {}, publishedAt = {}",
            responseDto.id(),
            responseDto.viewedBy(),
            responseDto.articleId(),
            responseDto.createdAt(),
            responseDto.articlePublishedDate()
        );

        return ResponseEntity.ok(responseDto);
    }



}
