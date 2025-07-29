package org.project.monewping.domain.interest.repository;

import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import java.util.UUID;
 
/**
 * 관심사 커스텀 레포지토리 인터페이스입니다.
 *
 * <p>커서 기반 목록 조회 등 확장 기능을 정의합니다.</p>
 */
public interface InterestRepositoryCustom {
    /**
     * 검색/정렬/커서 기반으로 관심사 목록을 조회합니다.
     *
     * @param request 검색/정렬/커서/사이즈 등 요청 DTO
     * @param monewRequestUserID 요청자 ID(구독 여부 등 추가 정보에 활용 가능)
     * @return 커서 페이지네이션 응답 DTO
     */
    CursorPageResponseInterestDto searchWithCursor(CursorPageRequestSearchInterestDto request, UUID monewRequestUserID);
} 