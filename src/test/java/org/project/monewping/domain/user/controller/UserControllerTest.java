package org.project.monewping.domain.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.service.UserService;
import org.project.monewping.global.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * {@link UserController}의 HTTP 요청 처리 로직을 테스트하는 클래스
 * 
 * <p>
 * 사용자 컨트롤러의 REST API 엔드포인트들을 단위 테스트로 검증합니다.
 * Mockito를 사용하여 서비스 계층을 모킹하고, 컨트롤러의 순수한 HTTP 처리 로직만을 테스트합니다.
 * </p>
 * 
 * <p>
 * 테스트 범위:
 * </p>
 * <ul>
 * <li>HTTP 요청/응답 처리</li>
 * <li>상태 코드 설정</li>
 * <li>서비스 계층 호출</li>
 * <li>예외 처리</li>
 * </ul>
 * 
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserRegisterRequest request;
    private UserRegisterResponse response;

    /**
     * 각 테스트 실행 전 공통 테스트 데이터를 설정합니다.
     * 
     * <p>
     * 테스트에서 사용할 요청 객체와 응답 객체를 미리 생성하여
     * 테스트 코드의 중복을 제거합니다.
     * </p>
     */
    @BeforeEach
    void setUp() {
        request = new UserRegisterRequest(
                "test@example.com",
                "testuser",
                "password123");

        response = new UserRegisterResponse(
                UUID.randomUUID(),
                "test@example.com",
                "testuser",
                Instant.now());
    }

    /**
     * 정상적인 회원가입 요청 처리를 테스트합니다.
     * 
     * <p>
     * 유효한 회원가입 요청이 들어왔을 때 201 Created 상태코드와 함께
     * 올바른 사용자 정보를 응답으로 반환하는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("정상적인 회원가입 요청 시 201 Created와 사용자 정보를 반환한다")
    void register_Success() {
        // given
        given(userService.register(any(UserRegisterRequest.class))).willReturn(response);

        // when
        ResponseEntity<UserRegisterResponse> result = userController.register(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().email()).isEqualTo(request.email());
        assertThat(result.getBody().nickname()).isEqualTo(request.nickname());
        assertThat(result.getBody().id()).isEqualTo(response.id());
        assertThat(result.getBody().createdAt()).isEqualTo(response.createdAt());

        verify(userService).register(request);
    }

    /**
     * 이메일 중복 시 예외가 발생하는지 테스트합니다.
     * 
     * <p>
     * 동일한 이메일을 가진 사용자가 이미 존재하는 경우
     * EmailAlreadyExistsException이 발생하는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("이메일이 중복된 경우 EmailAlreadyExistsException을 발생시킨다")
    void register_EmailAlreadyExists_ThrowsException() {
        // given
        given(userService.register(any(UserRegisterRequest.class)))
                .willThrow(new EmailAlreadyExistsException("이미 존재하는 이메일입니다."));

        // when & then
        assertThatThrownBy(() -> userController.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        verify(userService).register(request);
    }

    /**
     * 컨트롤러가 UserService를 올바르게 호출하는지 테스트합니다.
     * 
     * <p>
     * 회원가입 요청이 들어왔을 때 컨트롤러가 서비스의 register 메서드를
     * 올바른 파라미터로 호출하는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("Controller가 UserService를 올바르게 호출한다")
    void register_CallsUserService() {
        // given
        given(userService.register(any(UserRegisterRequest.class))).willReturn(response);

        // when
        userController.register(request);

        // then
        verify(userService).register(request);
    }

    /**
     * 응답에 올바른 HTTP 상태코드가 설정되는지 테스트합니다.
     * 
     * <p>
     * 회원가입 성공 시 201 Created 상태코드가
     * 올바르게 설정되는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("응답에 올바른 HTTP 상태코드를 설정한다")
    void register_ReturnsCorrectHttpStatus() {
        // given
        given(userService.register(any(UserRegisterRequest.class))).willReturn(response);

        // when
        ResponseEntity<UserRegisterResponse> result = userController.register(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    /**
     * 응답 바디에 서비스 결과가 포함되는지 테스트합니다.
     * 
     * <p>
     * UserService에서 반환한 결과가 응답 바디에
     * 올바르게 포함되는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("응답 바디에 UserService의 결과를 포함한다")
    void register_ReturnsServiceResult() {
        // given
        given(userService.register(any(UserRegisterRequest.class))).willReturn(response);

        // when
        ResponseEntity<UserRegisterResponse> result = userController.register(request);

        // then
        assertThat(result.getBody()).isEqualTo(response);
    }
}
