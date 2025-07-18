package org.project.monewping.domain.article.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;
import org.project.monewping.domain.article.service.ArticleRestoreService;
import org.project.monewping.domain.article.service.ArticleViewsService;
import org.project.monewping.domain.article.service.ArticlesService;
import org.project.monewping.global.dto.CursorPageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 뉴스 기사 조회 기록 등록 및 뉴스 기사 목록 조회를 위한 REST API 컨트롤러.
 * <p>
 * 사용자의 뉴스 기사 조회 기록 등록과 조건 기반 뉴스 기사 목록 조회 기능을 제공합니다.
 * </p>
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleViewsService articleViewsService;
    private final ArticlesService articlesService;
    private final ArticleRestoreService articleRestoreService;

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

        log.info("기사 조회 등록 완료 : id = {}, 조회한 사용자 ID = {}, 조회한 뉴스 기사의 ID = {}, 뉴스 기사 조회 시각 = {}, 조회한 뉴스 기사 제목 = {}",

            responseDto.id(),
            responseDto.viewedBy(),
            responseDto.articleId(),
            responseDto.createdAt(),
            responseDto.articleTitle()
        );

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 뉴스 기사 목록을 조건에 맞게 조회합니다.
     *
     * @param keyword       제목 또는 요약에 포함될 검색어 (선택)
     * @param interestId    관심사 ID (선택)
     * @param sourceIn      출처 목록 필터 (선택)
     * @param publishDateFrom 날짜 범위 시작 (선택)
     * @param publishDateTo   날짜 범위 끝 (선택)
     * @param orderBy       정렬 기준 (publishDate, commentCount, viewCount) - 필수
     * @param direction     정렬 방향 (ASC, DESC) - 필수
     * @param cursor        커서 ID (선택)
     * @param after         커서 보조 기준일자 (선택)
     * @param limit         페이지 크기 (필수, 최소 1)
     * @param userId  요청자 ID 헤더 "Monew-Request-User-ID" (필수)
     * @return 커서 기반 페이지네이션 결과
     */
    @GetMapping
    public CursorPageResponse<ArticleDto> getArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) UUID interestId,
        @RequestParam(required = false) List<String> sourceIn,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishDateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishDateTo,
        @RequestParam @NotBlank @Pattern(regexp = "publishDate|commentCount|viewCount", message = "정렬 조건은 날짜, 댓글 수, 조회 수 중 하나여야 합니다.") String orderBy,
        @RequestParam @NotBlank @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE, message = "정렬 방향은 ASC 또는 DESC이어야 합니다.") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
        @RequestParam @Min(1) int limit,
        @RequestHeader(name = "Monew-Request-User-ID") UUID userId
    ) {
        log.info("뉴스 기사 목록 조회 요청 : 키워드 = {}, 관심사 ID = {}, 뉴스 기사 출처 = {}, 기사 발행일 시작 범위 = {}, 기사 발행일 종료 범위 = {},"
                + " 정렬 기준 = {}, 정렬 방향 = {}, cursor = {}, after = {}, limit = {}, userId = {}",
            keyword, interestId, sourceIn, publishDateFrom, publishDateTo, orderBy, direction, cursor, after, limit, userId);

        ArticleSearchRequest searchRequest = new ArticleSearchRequest(
            keyword,
            interestId,
            sourceIn,
            publishDateFrom,
            publishDateTo,
            orderBy,
            direction,
            cursor,
            after,
            limit,
            userId
        );

        return articlesService.findArticles(searchRequest);
    }

    /**
     * 뉴스 기사 출처 목록을 조회합니다.
     *
     * @return 출처 문자열 목록
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getAllSources() {
        List<String> sources = articlesService.getAllSources();
        return ResponseEntity.ok(sources);
    }

    /**
     * 날짜 범위(from ~ to) 내의 뉴스 기사 복구 요청 처리
     */
    @GetMapping("/restore")
    public List<ArticleRestoreResultDto> restoreArticlesByDateRange(
        @RequestParam("from")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam("to")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
    ) {
        log.info("뉴스 기사 데이터 복구 요청 : from = {} to = {}", from, to);

        List<LocalDate> targetDates = from.datesUntil(to.plusDays(1))
            .toList();

        return targetDates.stream()
            .map(articleRestoreService::restoreArticlesByDate)
            .collect(Collectors.toList());
    }

}
