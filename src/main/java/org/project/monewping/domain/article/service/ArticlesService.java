package org.project.monewping.domain.article.service;

import java.util.List;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;
import org.project.monewping.domain.article.dto.request.ArticleSearchRequest;
import org.project.monewping.global.dto.CursorPageResponse;

public interface ArticlesService {

    // 단일 뉴스 기사 저장
    void save(ArticleSaveRequest request);

    // 여러 뉴스 기사 저장( 중복 Link 제외 )
    void saveAll(List<ArticleSaveRequest> requests);

    CursorPageResponse<ArticleDto> findArticles(ArticleSearchRequest request);

}
