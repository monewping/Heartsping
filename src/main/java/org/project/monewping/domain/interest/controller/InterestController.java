package org.project.monewping.domain.interest.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.dto.request.InterestRegisterRequest;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.exception.InterestCreationException;
import org.project.monewping.domain.interest.exception.SimilarInterestNameException;
import org.project.monewping.domain.interest.service.InterestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관심사 도메인의 REST 컨트롤러입니다.
 *
 * <p>관심사 생성 등 HTTP 요청을 처리하며,
 * 서비스 레이어와 연동해 결과를 반환합니다.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

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

    @GetMapping
    public ResponseEntity<CursorPageResponseInterestDto> findAll(
            @Valid @ModelAttribute CursorPageRequestSearchInterestDto request,
            @RequestHeader("Monew-Request-User-ID") @NotBlank String subscriberId
    ) {
        CursorPageResponseInterestDto response = interestService.findInterestByNameAndSubcriberCountByCursor(request, subscriberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
} 