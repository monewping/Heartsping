package org.project.monewping.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.user.dto.request.LoginRequest;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.response.LoginResponse;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.service.UserService;
import org.project.monewping.global.exception.EmailAlreadyExistsException;
import org.project.monewping.global.exception.LoginFailedException;
import org.project.monewping.global.exception.GlobalExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UserController의 웹 계층 슬라이스 테스트
 * 
 * <p>
 * 순수 Mockito를 사용하여 UserService를 모킹하고
 * MockMvc를 standalone으로 구성하여 Spring 컨텍스트 로딩 문제를 회피합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 슬라이스 테스트")
class UserControllerSliceTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("정상적인 회원가입 요청이 성공한다")
    void testSuccessfulUserRegistration() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@example.com",
                "testuser",
                "password123");

        UserRegisterResponse expectedResponse = new UserRegisterResponse(
                UUID.randomUUID(),
                "test@example.com",
                "testuser",
                Instant.now());

        given(userService.register(any(UserRegisterRequest.class)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("testuser"));
    }

    @Test
    @DisplayName("이메일 중복 시 409 Conflict를 반환한다")
    void testEmailAlreadyExists() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "duplicate@example.com",
                "testuser",
                "password123");

        given(userService.register(any(UserRegisterRequest.class)))
                .willThrow(new EmailAlreadyExistsException("이미 존재하는 이메일입니다: duplicate@example.com"));

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다: duplicate@example.com"));
    }

    @Test
    @DisplayName("유효하지 않은 이메일 형식으로 400 Bad Request를 반환한다")
    void testInvalidEmailFormat() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "invalid-email",
                "testuser",
                "password123");

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 닉네임으로 400 Bad Request를 반환한다")
    void testEmptyNickname() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@example.com",
                "",
                "password123");

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("짧은 비밀번호로 400 Bad Request를 반환한다")
    void testShortPassword() throws Exception {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@example.com",
                "testuser",
                "123");

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("null 필드가 있는 요청으로 400 Bad Request를 반환한다")
    void testNullFields() throws Exception {
        // given
        String jsonRequest = "{\"email\":null,\"nickname\":\"testuser\",\"password\":\"password123\"}";

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 JSON 형식으로 400 Bad Request를 반환한다")
    void testMalformedJson() throws Exception {
        // given
        String malformedJson = "{\"email\":\"test@example.com\",\"nickname\":\"testuser\",\"password\":}";

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== 로그인 관련 테스트 ==========

    @Test
    @DisplayName("정상적인 로그인 요청이 성공한다")
    void testSuccessfulLogin() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "password123");

        LoginResponse expectedResponse = new LoginResponse(
                UUID.randomUUID(),
                "test@example.com",
                "testuser",
                Instant.now());

        given(userService.login(any(LoginRequest.class)))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("testuser"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 401 Unauthorized를 반환한다")
    void testLoginWithNonExistentEmail() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "nonexistent@example.com",
                "password123");

        given(userService.login(any(LoginRequest.class)))
                .willThrow(new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 Unauthorized를 반환한다")
    void testLoginWithWrongPassword() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "wrongpassword");

        given(userService.login(any(LoginRequest.class)))
                .willThrow(new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("빈 이메일로 로그인 시 400 Bad Request를 반환한다")
    void testLoginWithEmptyEmail() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "",
                "password123");

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 비밀번호로 로그인 시 400 Bad Request를 반환한다")
    void testLoginWithEmptyPassword() throws Exception {
        // given
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "");

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("null 이메일로 로그인 시 400 Bad Request를 반환한다")
    void testLoginWithNullEmail() throws Exception {
        // given
        String jsonRequest = "{\"email\":null,\"password\":\"password123\"}";

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("null 비밀번호로 로그인 시 400 Bad Request를 반환한다")
    void testLoginWithNullPassword() throws Exception {
        // given
        String jsonRequest = "{\"email\":\"test@example.com\",\"password\":null}";

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 JSON 형식으로 로그인 시 400 Bad Request를 반환한다")
    void testLoginWithMalformedJson() throws Exception {
        // given
        String malformedJson = "{\"email\":\"test@example.com\",\"password\":}";

        // when & then
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
