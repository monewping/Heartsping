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

    /**
     * 특정 뉴스 기사에 대해 사용자의 조회 기록을 등록한다.
     *
     * @param articleId 조회할 뉴스 기사 ID (경로 변수)
     * @param viewedBy 요청 헤더 "Monew-Request-User-ID"에 포함된 사용자 ID
     * @return 등록된 조회 기록 정보 (ArticleViewDto)
     */
    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> registerArticleView(
        @PathVariable UUID articleId,
        @RequestHeader("Monew-Request-User-ID") UUID viewedBy
    ) {
        log.info("기사 조회 등록 요청 수신 : userId = {}, articleId = {}", viewedBy, articleId);

        // Service로 사용자 ID, 기사 ID 전달
        ArticleViewDto responseDto = articleViewsService.registerView(viewedBy, articleId);

        log.info("기사 조회 등록 완료 : id = {}, 조회자 ID = {}, 조회한 뉴스 기사 ID = {}, 조회 시각 = {}, 조회한 뉴스 기사 제목 = {}",
            responseDto.id(),
            responseDto.viewedBy(),
            responseDto.articleId(),
            responseDto.createdAt(),
            responseDto.articleTitle()
        );

        return ResponseEntity.ok(responseDto);
    }



}
