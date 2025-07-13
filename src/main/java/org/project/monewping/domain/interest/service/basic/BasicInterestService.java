package org.project.monewping.domain.interest.service.basic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.InterestRegisterRequest;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.mapper.InterestMapper;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.InterestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 관심사 비즈니스 로직을 구현하는 서비스입니다.
 *
 * <p>관심사 생성, 중복 체크, 키워드 관리 등
 * 관심사 도메인 핵심 기능을 제공합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicInterestService implements InterestService {

    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;

    /**
     * 관심사를 등록합니다.
     *
     * <p>요청 데이터의 유효성을 검증하고, 중복 여부를 확인한 뒤
     * 관심사와 키워드를 생성하여 저장합니다.</p>
     * @param request 관심사 등록 요청 DTO
     * @return 등록된 관심사 정보 DTO
     * @throws DuplicateInterestNameException 이미 동일한 이름의 관심사가 존재할 경우
     * @throws InterestCreationException 관심사 생성 중 예기치 못한 오류가 발생한 경우
     * @throws IllegalArgumentException 요청 데이터가 유효하지 않은 경우
     */
    @Override
    @Transactional
    public InterestDto create(InterestRegisterRequest request) {
        log.info("[InterestService] 관심사 등록 요청 시작: name={}, keywords={}", request.name(), request.keywords());
        
        validateRequest(request);
        
        log.debug("[InterestService] 관심사 이름 중복 확인: name={}", request.name());
        if (interestRepository.existsByName(request.name())) {
            log.warn("[InterestService] 중복된 관심사 이름으로 생성 시도: {}", request.name());
            throw new DuplicateInterestNameException(request.name());
        }
        log.debug("[InterestService] 관심사 이름 중복 확인 완료: name={}", request.name());

        try {
            // 빌더 패턴을 사용하여 Interest 엔티티 생성
            Interest interest = Interest.builder()
                    .name(request.name())
                    .subscriberCount(0L)
                    .build();
            
            log.debug("[InterestService] 관심사 저장 시작: name={}", interest.getName());
            Interest savedInterest = interestRepository.save(interest);
            log.debug("[InterestService] 관심사 저장 완료: id={}, name={}", savedInterest.getId(), savedInterest.getName());
            
            // 키워드 생성 및 연결
            if (request.keywords() != null && !request.keywords().isEmpty()) {
                List<Keyword> keywords = createKeywords(savedInterest, request.keywords());
                keywords.forEach(savedInterest::addKeyword);
                log.info("[InterestService] 관심사 등록 완료: id={}, name={}, keywords={}",
                        savedInterest.getId(), savedInterest.getName(), request.keywords());
            } else {
                log.info("[InterestService] 관심사 등록 완료 (키워드 없음): id={}, name={}",
                        savedInterest.getId(), savedInterest.getName());
            }
            
            return interestMapper.toDto(savedInterest);
        } catch (Exception e) {
            log.error("[InterestService] 관심사 등록 중 오류 발생: name={}, error={}", request.name(), e.getMessage(), e);
            throw new InterestCreationException("관심사 등록 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 관심사 생성 요청 데이터의 유효성을 검증합니다.
     *
     * <p>null, 빈 문자열, 길이 초과 여부를 확인합니다.</p>
     * @param requestDto 관심사 생성 요청 DTO
     * @throws IllegalArgumentException 요청 데이터가 유효하지 않은 경우
     */
    private void validateRequest(InterestRegisterRequest requestDto) {
        log.debug("[InterestService] 관심사 요청 유효성 검증 시작: {}", requestDto);
        
        if (requestDto == null) {
            log.error("[InterestService] 관심사 요청이 null입니다.");
            throw new IllegalArgumentException("관심사 요청은 null일 수 없습니다.");
        }

        if (!StringUtils.hasText(requestDto.name())) {
            log.error("[InterestService] 관심사 이름이 비어있습니다.");
            throw new IllegalArgumentException("관심사 이름은 필수입니다.");
        }

        if (requestDto.name().length() > 100) {
            log.error("[InterestService] 관심사 이름이 100자를 초과합니다: {}", requestDto.name().length());
            throw new IllegalArgumentException("관심사 이름은 100자를 초과할 수 없습니다.");
        }

        log.debug("[InterestService] 관심사 요청 유효성 검증 완료");
    }

    /**
     * 키워드 이름 리스트를 Keyword 엔티티 리스트로 변환합니다.
     *
     * <p>빈 문자열/NULL은 무시하고, 빌더 패턴으로 엔티티를 생성합니다.</p>
     * @param interest 관심사
     * @param keywordNames 키워드 이름 리스트
     * @return Keyword 엔티티 리스트
     */
    private List<Keyword> createKeywords(Interest interest, List<String> keywordNames) {
        log.debug("[InterestService] 키워드 생성 시작: interestId={}, keywordNames={}", interest.getId(), keywordNames);
        
        List<Keyword> keywords = new ArrayList<>();
        for (String keywordName : keywordNames) {
            if (StringUtils.hasText(keywordName)) {
                // 빌더 패턴을 사용하여 Keyword 엔티티 생성
                Keyword keyword = Keyword.builder()
                        .interest(interest)
                        .keyword(keywordName)
                        .build();
                keywords.add(keyword);
            }
        }
        
        log.debug("[InterestService] 키워드 생성 완료: {}개", keywords.size());
        return keywords;
    }
} 