package org.project.monewping.domain.interest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.request.InterestRegisterRequest;
import org.project.monewping.domain.interest.dto.request.InterestUpdateRequest;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.dto.SubscriptionDto;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.DuplicateKeywordException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.InterestNotFoundException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.domain.interest.service.InterestService;
import org.project.monewping.domain.interest.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 관심사 도메인의 REST 컨트롤러입니다.
 *
 * <p>관심사 등록, 목록 조회, 구독 등 HTTP 요청을 처리하며
 * 서비스 레이어와 연동해 결과를 반환합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;
    private final SubscriptionService subscriptionService;

    /**
     * 관심사를 등록하는 API입니다.
     *
     * <p>요청 데이터 유효성 검증 후 서비스에 위임하여
     * 관심사를 생성하고 201 응답을 반환합니다.</p>
     * @param request 관심사 등록 요청 DTO
     * @return 생성된 관심사 정보와 201 응답
     * @throws DuplicateInterestNameException 동일한 이름의 관심사가 이미 존재하는 경우
     * @throws SimilarInterestNameException 80% 이상 유사한 이름의 관심사가 존재하는 경우
     * @throws InterestCreationException 관심사 생성 중 오류가 발생한 경우
     * @throws IllegalArgumentException 요청 데이터가 유효하지 않은 경우
     */
    @PostMapping
    public ResponseEntity<InterestDto> create(@Valid @RequestBody InterestRegisterRequest request) {
        log.info("[InterestController] 관심사 등록 API 호출: name={}", request.name());
        
        try {
            InterestDto responseDto = interestService.create(request);
            
            log.info("[InterestController] 관심사 등록 API 성공: id={}, name={}", responseDto.id(), responseDto.name());
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Location", "/api/interests/" + responseDto.id())
                    .body(responseDto);
        } catch (Exception e) {
            log.error("[InterestController] 관심사 등록 API 실패: name={}, error={}", request.name(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 관심사 목록을 커서 기반으로 조회합니다.
     *
     * @param request 검색/정렬/커서/사이즈 등 요청 DTO
     * @param subscriberId 요청자(구독자) ID (구독여부 판단에 활용)
     * @return 커서 페이지네이션 응답 DTO
     */
    @GetMapping
    public ResponseEntity<CursorPageResponseInterestDto> findAll(
            @Valid @ModelAttribute CursorPageRequestSearchInterestDto request,
            @RequestHeader("Monew-Request-User-ID") UUID subscriberId
    ) {
        CursorPageResponseInterestDto response = interestService.findInterestByNameAndSubcriberCountByCursor(request, subscriberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    /**
     * 관심사 구독을 등록합니다.
     *
     * @param interestId 구독할 관심사 ID
     * @param subscriberId 구독자(사용자) ID
     * @return 구독 정보 DTO
     */
    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionDto> subscribe(
            @PathVariable UUID interestId,
            @RequestHeader("Monew-Request-User-ID") UUID subscriberId
    ) {
        SubscriptionDto response = subscriptionService.subscribe(interestId, subscriberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);

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
    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> update(
            @PathVariable UUID interestId,
            @Valid @RequestBody InterestUpdateRequest request
    ) {
        log.info("[InterestController] 관심사 키워드 수정 API 호출: interestId={}, keywords={}", 
                interestId, request.keywords());
        
        try {
            InterestDto responseDto = interestService.update(interestId, request);
            
            log.info("[InterestController] 관심사 키워드 수정 API 성공: interestId={}, keywords={}", 
                    interestId, responseDto.keywords());
            
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            log.error("[InterestController] 관심사 키워드 수정 API 실패: interestId={}, error={}", 
                    interestId, e.getMessage(), e);
            throw e;
        }
    }
} 