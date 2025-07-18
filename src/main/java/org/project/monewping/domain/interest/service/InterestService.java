package org.project.monewping.domain.interest.service;

import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.request.InterestRegisterRequest;
import org.project.monewping.domain.interest.dto.request.InterestUpdateRequest;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;

import java.util.UUID;

public interface InterestService {

    InterestDto create(InterestRegisterRequest request);
    CursorPageResponseInterestDto findInterestByNameAndSubcriberCountByCursor(CursorPageRequestSearchInterestDto request, UUID monewRequestUserID);
    InterestDto update(UUID interestId, InterestUpdateRequest request);
} 