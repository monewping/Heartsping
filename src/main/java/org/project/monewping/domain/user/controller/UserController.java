package org.project.monewping.domain.user.controller;

import org.project.monewping.domain.user.dto.request.LoginRequest;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.request.UserNicknameUpdateRequest;
import org.project.monewping.domain.user.dto.response.LoginResponse;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 사용자 관련 API를 처리하는 REST 컨트롤러
 *
 * <p>
 * 사용자 등록, 조회, 수정, 삭제 등의 HTTP 요청을 처리합니다.
 * </p>
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 사용자 회원가입을 처리합니다.
     *
     * <p>
     * 새로운 사용자를 등록하고 등록된 사용자 정보를 반환합니다.
     * 이메일 중복 시 예외가 발생하며, 유효성 검사를 통과해야 합니다.
     * </p>
     *
     * @param request 회원가입 요청 정보 (이메일, 닉네임, 비밀번호)
     * @return {@link ResponseEntity}&lt;{@link UserRegisterResponse}&gt;
     *         201 Created 상태코드와 함께 등록된 사용자 정보 반환
     * @throws org.project.monewping.global.exception.EmailAlreadyExistsException 이메일이
     *                                                                            이미
     *                                                                            존재하는
     *                                                                            경우
     * @throws org.springframework.web.bind.MethodArgumentNotValidException       유효성
     *                                                                            검사
     *                                                                            실패
     *                                                                            시
     *
     */
    @PostMapping("")
    public ResponseEntity<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        UserRegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 로그인을 처리합니다.
     *
     * <p>
     * 이메일과 비밀번호를 검증하여 사용자 인증을 수행합니다.
     * 로그인 성공 시 사용자 정보를 반환하고, 실패 시 예외가 발생합니다.
     * </p>
     *
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return {@link ResponseEntity}&lt;{@link LoginResponse}&gt;
     *         200 OK 상태코드와 함께 로그인된 사용자 정보 반환
     * @throws org.project.monewping.global.exception.LoginFailedException  이메일 또는
     *                                                                      비밀번호가
     *                                                                      일치하지 않는
     *                                                                      경우
     * @throws org.springframework.web.bind.MethodArgumentNotValidException 유효성
     *                                                                      검사
     *                                                                      실패
     *                                                                      시
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserRegisterResponse> updateNickname(
            @PathVariable("userId") String userId,
            @Valid @RequestBody UserNicknameUpdateRequest request) {
        UserRegisterResponse response = userService.updateNickname(UUID.fromString(userId), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자를 논리적으로 삭제합니다.
     *
     * <p>
     * 사용자와 관련된 모든 데이터를 논리적으로 삭제 처리합니다.
     * 실제 데이터는 유지하되 삭제 표시를 합니다.
     * </p>
     *
     * @param userId 삭제할 사용자 ID
     * @param requesterId 삭제 요청자 ID (헤더에서 추출)
     * @return HTTP 204 (No Content) 논리 삭제 성공 시
     * @throws org.project.monewping.domain.user.exception.UserNotFoundException 사용자를 찾을 수 없는 경우 404 반환
     * @throws org.project.monewping.domain.user.exception.UserDeleteException 삭제 권한이 없는 경우 403 반환
     * @throws org.project.monewping.domain.user.exception.UserAlreadyDeletedException 이미 삭제된 사용자인 경우 404 반환
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> softDelete(
        @PathVariable UUID userId,
        @RequestHeader("Monew-Request-User-ID") UUID requesterId) {
    log.info("사용자 논리 삭제 요청: userId={}, requesterId={}", userId, requesterId);
    userService.softDelete(userId, requesterId);
    return ResponseEntity.noContent().build();
    }

    /**
     * 사용자를 물리적으로 삭제합니다.
     *
     * <p>
     * 사용자와 관련된 모든 데이터를 물리적으로 삭제합니다.
     * </p>
     *
     * @param userId 삭제할 사용자 ID
     * @param requesterId 삭제 요청자 ID (헤더에서 추출)
     * @return HTTP 204 (No Content) 물리 삭제 성공 시
     * @throws org.project.monewping.domain.user.exception.UserNotFoundException 사용자를 찾을 수 없는 경우 404 반환
     * @throws org.project.monewping.domain.user.exception.UserDeleteException 삭제 권한이 없는 경우 403 반환
     */
    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> hardDelete(
        @PathVariable UUID userId,
        @RequestHeader("Monew-Request-User-ID") UUID requesterId) {
    log.info("사용자 물리 삭제 요청: userId={}, requesterId={}", userId, requesterId);
    userService.hardDelete(userId, requesterId);
    return ResponseEntity.noContent().build();
  }
}
