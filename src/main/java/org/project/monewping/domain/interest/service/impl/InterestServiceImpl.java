package org.project.monewping.domain.interest.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.request.InterestRegisterRequest;
import org.project.monewping.domain.interest.dto.request.InterestUpdateRequest;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.DuplicateKeywordException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.InterestDeletionException;
import org.project.monewping.domain.interest.exception.InterestNotFoundException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.domain.interest.mapper.InterestMapper;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.InterestService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 관심사 비즈니스 로직을 구현하는 서비스입니다.
 *
 * <p>관심사 생성, 중복 체크, 키워드 관리 등 도메인 핵심 기능과
 * 검색/정렬/구독자 수 기반 커서 페이지네이션 등 실무에서 요구되는
 * 다양한 관심사 목록 조회 기능을 제공합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestServiceImpl implements InterestService {

    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;
    private static final String SERVICE_NAME = "[InterestService] ";

    private static final double SIMILARITY_THRESHOLD = 0.8; // 80% 유사도 임계값
    private static final JaroWinklerSimilarity JW = new JaroWinklerSimilarity();

    /**
     * 관심사를 등록합니다.
     *
     * <p>요청 데이터의 유효성을 검증하고, 중복 여부를 확인한 뒤
     * 관심사와 키워드를 생성하여 저장합니다.</p>
     * @param request 관심사 등록 요청 DTO
     * @return 등록된 관심사 정보 DTO
     * @throws DuplicateInterestNameException 이미 동일한 이름의 관심사가 존재할 경우
     * @throws SimilarInterestNameException 80% 이상 유사한 이름의 관심사가 존재할 경우
     * @throws InterestCreationException 관심사 생성 중 예기치 못한 오류가 발생한 경우
     * @throws IllegalArgumentException 요청 데이터가 유효하지 않은 경우
     */
    @Override
    @Transactional
    public InterestDto create(InterestRegisterRequest request) {
        log.info("[InterestService] 관심사 등록 요청: name={}", request.name());

        try {
            validateRequest(request);
        } catch (IllegalArgumentException e) {
            log.warn("[InterestService] 유효성 검증 실패: name={}, error={}", request.name(), e.getMessage());
            throw e;
        }

        if (interestRepository.existsByName(request.name())) {
            log.warn("[InterestService] 중복된 관심사 이름: {}", request.name());
            throw new DuplicateInterestNameException(request.name());
        }

        List<String> similarNames = findSimilarInterestNames(request.name());
        if (!similarNames.isEmpty()) {
            log.warn("[InterestService] 유사한 관심사 이름: {} (유사: {})", request.name(), similarNames);
            throw new SimilarInterestNameException(request.name(), String.join(", ", similarNames));
        }

        try {
            Interest interest = Interest.builder()
                    .name(request.name())
                    .subscriberCount(0L)
                    .build();

            Interest savedInterest = interestRepository.save(interest);

            if (request.keywords() != null && !request.keywords().isEmpty()) {
                List<Keyword> keywords = createKeywords(request.keywords());
                keywords.forEach(savedInterest::addKeyword);
                log.info("[InterestService] 관심사 등록 성공: name={}, keywords={}", savedInterest.getName(), request.keywords());
            } else {
                log.info("[InterestService] 관심사 등록 성공(키워드 없음): name={}", savedInterest.getName());
            }

            return interestMapper.toDto(savedInterest);
        } catch (Exception e) {
            log.error("[InterestService] 관심사 등록 실패: name={}, error={}", request.name(), e.getMessage(), e);
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
        if (requestDto == null) {
            throw new IllegalArgumentException("관심사 요청은 null일 수 없습니다.");
        }

        if (!StringUtils.hasText(requestDto.name())) {
            throw new IllegalArgumentException("관심사 이름은 필수입니다.");
        }

        if (requestDto.name().length() > 100) {
            throw new IllegalArgumentException("관심사 이름은 100자를 초과할 수 없습니다.");
        }
    }

    /**
     * 키워드 이름 리스트를 Keyword 엔티티 리스트로 변환합니다.
     *
     * <p>null, 빈 문자열은 무시하고, 빌더 패턴으로 엔티티를 생성합니다.</p>
     * @param keywordNames 키워드 이름 리스트
     * @return Keyword 엔티티 리스트
     */
    private List<Keyword> createKeywords(List<String> keywordNames) {
        List<Keyword> keywords = new ArrayList<>();
        if (keywordNames == null) return keywords;
        
        for (String keywordName : keywordNames) {
            if (keywordName == null) continue;
            String trimmed = keywordName.trim();
            if (!StringUtils.hasText(trimmed)) continue;
            
            Keyword keyword = Keyword.builder()
                    .name(trimmed)
                    .build();
            keywords.add(keyword);
        }
        
        return keywords;
    }

    /**
     * Jaro-Winkler 유사도를 사용하여 80% 이상 유사한 관심사 이름을 찾습니다.
     *
     * <p>모든 기존 관심사 이름과 비교하여 유사도가 0.8 이상인 것을 반환합니다.</p>
     * @param newName 새로운 관심사 이름
     * @return 유사한 관심사 이름 리스트
     */
    private List<String> findSimilarInterestNames(String newName) {
        List<String> allNames = interestRepository.findAllNames();
        
        return allNames.stream()
                .filter(existingName -> calculateSimilarity(newName, existingName) >= SIMILARITY_THRESHOLD)
                .collect(Collectors.toList());
    }

    /**
     * Jaro-Winkler 유사도를 계산합니다.
     * <p>두 문자열 간의 유사도를 0.0~1.0 사이의 값으로 반환합니다.</p>
     */
    private double calculateSimilarity(String s1, String s2) {
        double score = JW.apply(s1, s2);
        log.info("[InterestService] 유사도 계산: '{}' vs '{}' = {}", s1, s2, score);
        return score;
    }

    /**
     * 검색어(관심사 이름/키워드), 정렬, 구독자 수 기준 커서 페이지네이션으로 관심사 목록을 조회합니다.
     *
     * - 검색어(keyword): 관심사 이름 또는 키워드에 부분일치하는 항목 조회
     * - 정렬(orderBy, direction): 이름/구독자수 기준 오름차순·내림차순 정렬
     * - 커서(cursor, after): 커서 기반 페이지네이션(무한 스크롤 등)에 사용
     * - limit: 페이지 크기(최대 100)
     * - monewRequestUserID: 요청자 ID(구독 여부 등 추가 정보에 활용)
     *
     * @param request 검색/정렬/커서/사이즈 등 요청 DTO
     * @param monewRequestUserID 요청자 ID(구독 여부 등 추가 정보에 활용 가능)
     * @return 커서 페이지네이션 응답 DTO (관심사 목록, 다음 커서 등 포함)
     * @throws IllegalArgumentException 커서 값이 잘못된 형식일 경우 등
     */
    @Override
    @Transactional
    public CursorPageResponseInterestDto findInterestByNameAndSubcriberCountByCursor(CursorPageRequestSearchInterestDto request, UUID monewRequestUserID) {
        return interestRepository.searchWithCursor(request, monewRequestUserID);
    }

    /**
     * 관심사의 키워드를 수정합니다.
     *
     * <p>관심사 이름은 수정할 수 없고, 키워드만 수정 가능합니다.
     * 기존 키워드를 모두 제거하고 새로운 키워드 목록으로 교체합니다.</p>
     * @param interestId 수정할 관심사 ID
     * @param request 키워드 수정 요청 DTO
     * @return 수정된 관심사 정보 DTO
     * @throws InterestNotFoundException 존재하지 않는 관심사 ID인 경우
     * @throws DuplicateKeywordException 중복된 키워드가 있는 경우
     * @throws IllegalArgumentException 요청 데이터가 유효하지 않은 경우
     */
    @Override
    @Transactional
    public InterestDto update(UUID interestId, InterestUpdateRequest request) {
        log.info("[InterestService] 관심사 키워드 수정 요청: interestId={}, keywords={}", interestId, request.keywords());

        // 키워드 유효성 검증을 먼저 수행
        validateKeywords(request.keywords());

        // 관심사 존재 여부 확인
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("[InterestService] 존재하지 않는 관심사: {}", interestId);
                    return new InterestNotFoundException(interestId);
                });

        try {
            // 키워드 업데이트
            interest.updateKeywords(request.keywords());
            Interest savedInterest = interestRepository.save(interest);
            
            log.info("[InterestService] 관심사 키워드 수정 성공: interestId={}, keywords={}", 
                    interestId, request.keywords());
            
            return interestMapper.toDto(savedInterest);
        } catch (Exception e) {
            log.error("[InterestService] 관심사 키워드 수정 실패: interestId={}, error={}", 
                    interestId, e.getMessage(), e);
            throw new InterestCreationException("관심사 키워드 수정 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 키워드 목록의 유효성을 검증합니다.
     *
     * <p>null, 빈 리스트, 중복된 키워드가 있는지 확인합니다.</p>
     * @param keywords 검증할 키워드 목록
     * @throws IllegalArgumentException 키워드가 null이거나 빈 리스트인 경우
     * @throws DuplicateKeywordException 중복된 키워드가 있는 경우
     */
    private void validateKeywords(List<String> keywords) {
        if (keywords == null) {
            throw new IllegalArgumentException("키워드는 필수입니다.");
        }
        
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("키워드는 1개 이상 10개 이하로 입력해야 합니다.");
        }
        
        // null이나 빈 문자열 제거 후 중복 검사
        List<String> validKeywords = keywords.stream()
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .map(String::trim)
                .toList();
        
        if (validKeywords.isEmpty()) {
            throw new IllegalArgumentException("키워드는 1개 이상 10개 이하로 입력해야 합니다.");
        }
        
        // 중복된 키워드 확인
        List<String> duplicates = validKeywords.stream()
                .filter(keyword -> validKeywords.stream()
                        .filter(k -> k.equals(keyword))
                        .count() > 1)
                .distinct()
                .toList();
        
        if (!duplicates.isEmpty()) {
            log.warn("[InterestService] 중복된 키워드 발견: {}", duplicates);
            throw new DuplicateKeywordException(duplicates.get(0));

    /**
     * 관심사를 삭제합니다.
     *
     * <p>관심사가 존재하는지 확인한 후 물리적으로 삭제합니다.
     * 관심사와 연관된 키워드도 함께 삭제됩니다.</p>
     * @param interestId 삭제할 관심사 ID
     * @throws InterestNotFoundException 관심사를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void delete(UUID interestId) {
        log.info("[InterestService] 관심사 삭제 요청: interestId={}", interestId);

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("[InterestService] 관심사를 찾을 수 없음: interestId={}", interestId);
                    return new InterestNotFoundException(interestId);
                });

        try {
            interestRepository.delete(interest);
            log.info("[InterestService] 관심사 삭제 성공: interestId={}, name={}", interestId, interest.getName());
        } catch (Exception e) {
            log.error("[InterestService] 관심사 삭제 실패: interestId={}, error={}", interestId, e.getMessage(), e);
            throw new InterestDeletionException("관심사 삭제 중 오류가 발생했습니다.", e);
        }
    }
}