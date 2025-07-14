package org.project.monewping.domain.article.controller;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.service.ArticleViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleViewService articleViewService;

    // 뉴스 기사 뷰 등록
    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> registerArticleView(
        @PathVariable UUID articleId,
        @RequestHeader("Monew-Request-User-ID") UUID viewedBy
    ) {
        log.info("기사 조회 등록 요청 수신 : userId = {}, articleId = {}", viewedBy, articleId);

        LocalDateTime articlePublishedDate = LocalDateTime.now();

        ArticleViewDto requestDto = new ArticleViewDto(
            UUID.randomUUID(),
            viewedBy,
            articleId,
            articlePublishedDate
        );

        ArticleViewDto responseDto = articleViewService.registerView(requestDto);
        log.info("기사 조회 등록 완료 : id = {}, userId = {}, articleId = {}, publishedDate = {}",
            requestDto.id(),
            responseDto.viewedBy(),
            responseDto.articleId(),
            responseDto.articlePublishedDate());

        return ResponseEntity.ok(responseDto);
    }



}
