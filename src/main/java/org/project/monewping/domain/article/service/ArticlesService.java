package org.project.monewping.domain.article.service;

import java.util.List;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;

public interface ArticlesService {

    // 단일 뉴스 기사 저장
    void save(ArticleSaveRequest request);

    // 여러 뉴스 기사 저장( 중복 Link 제외 )
    void saveAll(List<ArticleSaveRequest> requests);

}
