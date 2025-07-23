package org.project.monewping.domain.article.fetcher;

import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.article.dto.request.ArticleSaveRequest;

/**
 * 외부 뉴스 기사 수집기의 공통 인터페이스
 */
public interface ArticleFetcher {

    /**
     * 키워드를 기반으로 외부에서 뉴스 기사를 수집합니다.
     *
     * @param keywords 검색 키워드 (예: "인공지능", "주식")
     * @return 수집된 기사 리스트
     */
    List<ArticleSaveRequest> fetch(UUID interestId, List<String> keywords);

}