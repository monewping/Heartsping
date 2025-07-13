package org.project.monewping.domain.interest.service;

import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.InterestRegisterRequest;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;

/**
 * 관심사 관련 비즈니스 로직을 제공하는 서비스 인터페이스입니다.
 *
 * <p>관심사 생성, 조회, 수정, 삭제 등 도메인 핵심 로직을 정의합니다.</p>
 */
public interface InterestService {

    /**
     * 관심사를 등록합니다.
     *
     * <p>요청 DTO를 받아 유효성 검증 및 중복 체크 후
     * 관심사 정보를 저장합니다.</p>
     * @param request 관심사 등록 요청 DTO
     * @return 등록된 관심사 정보 DTO
     * @throws DuplicateInterestNameException 이미 동일한 이름의 관심사가 존재할 경우
     * @throws InterestCreationException 관심사 생성 중 예기치 못한 오류가 발생한 경우
     */
    InterestDto create(InterestRegisterRequest request);
} 