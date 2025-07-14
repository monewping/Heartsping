package org.project.monewping.domain.interest.service.basic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.InterestRegisterRequest;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.domain.interest.mapper.InterestMapper;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.InterestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final double SIMILARITY_THRESHOLD = 0.8; // 80% 유사도 임계값

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
                List<Keyword> keywords = createKeywords(savedInterest, request.keywords());
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
     * @param interest 관심사
     * @param keywordNames 키워드 이름 리스트
     * @return Keyword 엔티티 리스트
     */
    private List<Keyword> createKeywords(Interest interest, List<String> keywordNames) {
        List<Keyword> keywords = new ArrayList<>();
        if (keywordNames == null) return keywords;
        
        for (String keywordName : keywordNames) {
            if (keywordName == null) continue;
            String trimmed = keywordName.trim();
            if (!StringUtils.hasText(trimmed)) continue;
            
            Keyword keyword = Keyword.builder()
                    .interest(interest)
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
        if (s1.equals(s2)) {
            log.info("[InterestService] 유사도 계산: '{}' vs '{}' = 1.0 (동일)", s1, s2);
            return 1.0;
        }
        // Jaro 거리 계산
        double jaroDistance = calculateJaroDistance(s1, s2);
        // Jaro-Winkler 보정 적용
        double jaroWinklerDistance = calculateJaroWinklerDistance(s1, s2, jaroDistance);
        log.info("[InterestService] 유사도 계산: '{}' vs '{}' = {}", s1, s2, jaroWinklerDistance);
        return jaroWinklerDistance;
    }

    /**
     * Jaro 거리를 계산합니다.
     */
    private double calculateJaroDistance(String s1, String s2) {
        if (s1.length() == 0 && s2.length() == 0) {
            return 1.0;
        }
        int matchDistance = Math.max(s1.length(), s2.length()) / 2 - 1;
        if (matchDistance < 0) {
            matchDistance = 0;
        }
        boolean[] s1Matches = new boolean[s1.length()];
        boolean[] s2Matches = new boolean[s2.length()];
        int matches = 0;
        int transpositions = 0;
        // 첫 번째 문자열에서 매치 찾기
        for (int i = 0; i < s1.length(); i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, s2.length());
            for (int j = start; j < end; j++) {
                if (s2Matches[j] || s1.charAt(i) != s2.charAt(j)) {
                    continue;
                }
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) {
            return 0.0;
        }
        // 전치(transposition) 계산
        int k = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (!s1Matches[i]) {
                continue;
            }
            while (!s2Matches[k]) {
                k++;
            }
            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        double jaroDistance = (matches / (double) s1.length() + 
                matches / (double) s2.length() + 
                (matches - transpositions / 2.0) / matches) / 3.0;
        return jaroDistance;
    }

    /**
     * Jaro-Winkler 보정 적용
     */
    private double calculateJaroWinklerDistance(String s1, String s2, double jaroDistance) {
        if (jaroDistance < 0.7) {
            return jaroDistance;
        }
        int prefixLength = 0;
        int maxPrefixLength = Math.min(4, Math.min(s1.length(), s2.length()));
        for (int i = 0; i < maxPrefixLength; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefixLength++;
            } else {
                break;
            }
        }
        double jaroWinklerDistance = jaroDistance + 0.1 * prefixLength * (1.0 - jaroDistance);
        return jaroWinklerDistance;
    }
} 