package org.project.monewping.domain.interest.repository;

import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
 
public interface InterestRepositoryCustom {
    CursorPageResponseInterestDto searchWithCursor(CursorPageRequestSearchInterestDto request, String monewRequestUserID);
} 