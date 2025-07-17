package org.project.monewping.domain.user.controller;

import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * 사용자 관련 API를 처리하는 REST 컨트롤러
 *
 * <p>
 * 사용자 등록, 조회, 수정, 삭제 등의 HTTP 요청을 처리합니다.
 * </p>
 *
 */
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

    // ===================== [임시] 로그인 메서드 =====================
    /**
     * [임시] 사용자 로그인 API
     *
     * <p>
     * 이메일과 비밀번호가 일치하면 200 OK와 사용자 정보를 반환합니다.
     * 일치하지 않으면 400 Bad Request와 에러 메시지를 반환합니다.
     * (실무에서는 반드시 비밀번호 암호화/토큰 발급 등 필요)
     * </p>
     *
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return 200 OK + User 정보 or 400 Bad Request
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        // [임시] 실제로는 DTO를 사용하는 것이 좋음
        String email = request.get("email");
        String password = request.get("password");
        try {
            // [임시] UserService의 login도 임시임을 명시
            var user = userService.login(email, password); // [임시]
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // ===================== [임시] 로그인 메서드 끝 =====================
}
