package org.project.monewping.domain.user.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.dto.request.LoginRequest;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.response.LoginResponse;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.mapper.UserMapper;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.exception.EmailAlreadyExistsException;
import org.project.monewping.global.exception.LoginFailedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * {@link UserService}의 비즈니스 로직을 테스트하는 클래스
 * 
 * <p>
 * 사용자 서비스의 회원가입 및 로그인 관련 메서드들을 단위 테스트로 검증합니다.
 * Mockito를 사용하여 의존성을 모킹하고, 순수한 비즈니스 로직만을 테스트합니다.
 * </p>
 * 
 * <p>
 * 테스트 범위:
 * </p>
 * <ul>
 * <li>정상적인 회원가입 처리</li>
 * <li>이메일 중복 시 예외 처리</li>
 * <li>정상적인 로그인 처리</li>
 * <li>로그인 실패 시 예외 처리</li>
 * <li>유효성 검사 로직</li>
 * <li>데이터 변환 로직</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User userToSave;
    private User savedUser;
    private UserRegisterResponse registerResponse;
    private LoginResponse loginResponse;

    /**
     * 각 테스트 실행 전 공통 테스트 데이터를 설정합니다.
     * 
     * <p>
     * 테스트에서 사용할 UserRegisterRequest와 User 객체를 미리 생성하여
     * 테스트 코드의 중복을 제거합니다.
     * </p>
     */
    @BeforeEach
    void setUp() {
        registerRequest = new UserRegisterRequest(
                "test@example.com",
                "testuser",
                "password123");

        loginRequest = new LoginRequest(
                "test@example.com",
                "password123");

        userToSave = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword123")
                .build();

        savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword123")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        registerResponse = new UserRegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getCreatedAt());

        loginResponse = new LoginResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getCreatedAt());
    }

    /**
     * 정상적인 회원가입 처리 과정을 테스트합니다.
     * 
     * <p>
     * 이메일 중복 검사를 통과하고, 사용자 정보가 정상적으로 저장되어
     * 올바른 응답 객체가 반환되는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("정상적인 회원가입 시 사용자 정보를 반환한다")
    void register_Success() {
        // given
        given(userRepository.existsByEmail(registerRequest.email())).willReturn(false);
        given(userMapper.toEntity(registerRequest)).willReturn(userToSave);
        given(userRepository.save(userToSave)).willReturn(savedUser);
        given(userMapper.toResponse(savedUser)).willReturn(registerResponse);

        // when
        UserRegisterResponse result = userService.register(registerRequest);

        // then
        assertThat(result.email()).isEqualTo(registerRequest.email());
        assertThat(result.nickname()).isEqualTo(registerRequest.nickname());
        assertThat(result.id()).isEqualTo(savedUser.getId());
        assertThat(result.createdAt()).isEqualTo(savedUser.getCreatedAt());

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userMapper).toEntity(registerRequest);
        verify(userRepository).save(userToSave);
        verify(userMapper).toResponse(savedUser);
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
    @DisplayName("이메일이 이미 존재하는 경우 EmailAlreadyExistsException을 발생시킨다")
    void register_EmailAlreadyExists_ThrowsException() {
        // given
        given(userRepository.existsByEmail(registerRequest.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        verify(userRepository).existsByEmail(registerRequest.email());
    }

    /**
     * 이메일 중복 검사가 올바르게 수행되는지 테스트합니다.
     *
     * <p>
     * 회원가입 과정에서 이메일 중복 검사가 정상적으로 호출되는지 확인합니다.
     * </p>
     */
    @Test
    @DisplayName("이메일 중복 검사가 올바르게 수행된다")
    void validateEmailNotExists_Success() {
        // given
        given(userRepository.existsByEmail(registerRequest.email())).willReturn(false);
        given(userMapper.toEntity(registerRequest)).willReturn(userToSave);
        given(userRepository.save(userToSave)).willReturn(savedUser);
        given(userMapper.toResponse(savedUser)).willReturn(registerResponse);

        // when
        userService.register(registerRequest);

        // then
        verify(userRepository).existsByEmail(registerRequest.email());
    }

    /**
     * UserMapper가 올바르게 호출되는지 테스트합니다.
     *
     * <p>
     * UserRegisterRequest가 User 엔티티로 변환되고,
     * User 엔티티가 UserRegisterResponse로 변환되는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("UserMapper가 올바르게 호출된다")
    void register_CallsUserMapper() {
        // given
        given(userRepository.existsByEmail(registerRequest.email())).willReturn(false);
        given(userMapper.toEntity(registerRequest)).willReturn(userToSave);
        given(userRepository.save(userToSave)).willReturn(savedUser);
        given(userMapper.toResponse(savedUser)).willReturn(registerResponse);

        // when
        userService.register(registerRequest);

        // then
        verify(userMapper).toEntity(registerRequest);
        verify(userMapper).toResponse(savedUser);
    }

    /**
     * 정상적인 로그인 처리 과정을 테스트합니다.
     * 
     * <p>
     * 이메일로 사용자를 찾고, 비밀번호가 일치하는 경우
     * 올바른 로그인 응답 객체가 반환되는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("정상적인 로그인 시 사용자 정보를 반환한다")
    void login_Success() {
        // given
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).willReturn(true);
        given(userMapper.toLoginResponse(savedUser)).willReturn(loginResponse);

        // when
        LoginResponse result = userService.login(loginRequest);

        // then
        assertThat(result.email()).isEqualTo(loginRequest.email());
        assertThat(result.nickname()).isEqualTo(savedUser.getNickname());
        assertThat(result.id()).isEqualTo(savedUser.getId());
        assertThat(result.createdAt()).isEqualTo(savedUser.getCreatedAt());

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.password(), savedUser.getPassword());
        verify(userMapper).toLoginResponse(savedUser);
    }

    /**
     * 존재하지 않는 이메일로 로그인 시 예외가 발생하는지 테스트합니다.
     * 
     * <p>
     * 데이터베이스에 존재하지 않는 이메일로 로그인을 시도하는 경우
     * LoginFailedException이 발생하는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 LoginFailedException을 발생시킨다")
    void login_EmailNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");

        verify(userRepository).findByEmail(loginRequest.email());
    }

    /**
     * 잘못된 비밀번호로 로그인 시 예외가 발생하는지 테스트합니다.
     * 
     * <p>
     * 이메일은 존재하지만 비밀번호가 일치하지 않는 경우
     * LoginFailedException이 발생하는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 LoginFailedException을 발생시킨다")
    void login_WrongPassword_ThrowsException() {
        // given
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");

        verify(userRepository).findByEmail(loginRequest.email());
        verify(passwordEncoder).matches(loginRequest.password(), savedUser.getPassword());
    }

    /**
     * 비밀번호 검증 로직이 올바르게 동작하는지 테스트합니다.
     * 
     * <p>
     * PasswordEncoder를 통해 비밀번호 검증이 정상적으로 수행되는지 확인합니다.
     * </p>
     */
    @Test
    @DisplayName("비밀번호 검증 로직이 올바르게 동작한다")
    void login_PasswordVerification_Success() {
        // given
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).willReturn(true);
        given(userMapper.toLoginResponse(savedUser)).willReturn(loginResponse);

        // when
        userService.login(loginRequest);

        // then
        verify(passwordEncoder).matches(loginRequest.password(), savedUser.getPassword());
    }

    /**
     * 로그인 시 UserMapper가 올바르게 호출되는지 테스트합니다.
     *
     * <p>
     * User 엔티티가 LoginResponse로 변환되는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("로그인 시 UserMapper가 올바르게 호출된다")
    void login_CallsUserMapper() {
        // given
        given(userRepository.findByEmail(loginRequest.email())).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(loginRequest.password(), savedUser.getPassword())).willReturn(true);
        given(userMapper.toLoginResponse(savedUser)).willReturn(loginResponse);

        // when
        userService.login(loginRequest);

        // then
        verify(userMapper).toLoginResponse(savedUser);
    }

    /**
     * 빈 이메일로 로그인 시도 시 예외가 발생하는지 테스트합니다.
     */
    @Test
    @DisplayName("빈 이메일로 로그인 시도 시 LoginFailedException을 발생시킨다")
    void login_EmptyEmail_ThrowsException() {
        // given
        LoginRequest emptyEmailRequest = new LoginRequest("", "password123");

        // when & then
        assertThatThrownBy(() -> userService.login(emptyEmailRequest))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    /**
     * 빈 비밀번호로 로그인 시도 시 예외가 발생하는지 테스트합니다.
     */
    @Test
    @DisplayName("빈 비밀번호로 로그인 시도 시 LoginFailedException을 발생시킨다")
    void login_EmptyPassword_ThrowsException() {
        // given
        LoginRequest emptyPasswordRequest = new LoginRequest("test@example.com", "");

        // when & then
        assertThatThrownBy(() -> userService.login(emptyPasswordRequest))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    /**
     * null 이메일로 로그인 시도 시 예외가 발생하는지 테스트합니다.
     */
    @Test
    @DisplayName("null 이메일로 로그인 시도 시 LoginFailedException을 발생시킨다")
    void login_NullEmail_ThrowsException() {
        // given
        LoginRequest nullEmailRequest = new LoginRequest(null, "password123");

        // when & then
        assertThatThrownBy(() -> userService.login(nullEmailRequest))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    /**
     * null 비밀번호로 로그인 시도 시 예외가 발생하는지 테스트합니다.
     */
    @Test
    @DisplayName("null 비밀번호로 로그인 시도 시 LoginFailedException을 발생시킨다")
    void login_NullPassword_ThrowsException() {
        // given
        LoginRequest nullPasswordRequest = new LoginRequest("test@example.com", null);

        // when & then
        assertThatThrownBy(() -> userService.login(nullPasswordRequest))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}