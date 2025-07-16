package org.project.monewping.domain.user.service;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.mapper.UserMapper;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.exception.EmailAlreadyExistsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * {@link UserService}의 비즈니스 로직을 테스트하는 클래스
 * 
 * <p>
 * 사용자 서비스의 회원가입 관련 메서드들을 단위 테스트로 검증합니다.
 * Mockito를 사용하여 의존성을 모킹하고, 순수한 비즈니스 로직만을 테스트합니다.
 * </p>
 * 
 * <p>
 * 테스트 범위:
 * </p>
 * <ul>
 * <li>정상적인 회원가입 처리</li>
 * <li>이메일 중복 시 예외 처리</li>
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

  @InjectMocks
  private UserService userService;

  private UserRegisterRequest request;
  private User userToSave;
  private User savedUser;
  private UserRegisterResponse response;

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
    request = new UserRegisterRequest(
        "test@example.com",
        "testuser",
        "password123");

    userToSave = User.builder()
        .email("test@example.com")
        .nickname("testuser")
        .password("password123")
        .build();

    savedUser = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .nickname("testuser")
        .password("password123")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    response = new UserRegisterResponse(
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
    given(userRepository.existsByEmail(request.email())).willReturn(false);
    given(userMapper.toEntity(request)).willReturn(userToSave);
    given(userRepository.save(userToSave)).willReturn(savedUser);
    given(userMapper.toResponse(savedUser)).willReturn(response);

    // when
    UserRegisterResponse result = userService.register(request);

    // then
    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.nickname()).isEqualTo(request.nickname());
    assertThat(result.id()).isEqualTo(savedUser.getId());
    assertThat(result.createdAt()).isEqualTo(savedUser.getCreatedAt());

    verify(userRepository).existsByEmail(request.email());
    verify(userMapper).toEntity(request);
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
    given(userRepository.existsByEmail(request.email())).willReturn(true);

    // when & then
    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(EmailAlreadyExistsException.class)
        .hasMessage("이미 존재하는 이메일입니다.");

    verify(userRepository).existsByEmail(request.email());
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
    given(userRepository.existsByEmail(request.email())).willReturn(false);
    given(userMapper.toEntity(request)).willReturn(userToSave);
    given(userRepository.save(userToSave)).willReturn(savedUser);
    given(userMapper.toResponse(savedUser)).willReturn(response);

    // when
    userService.register(request);

    // then
    verify(userRepository).existsByEmail(request.email());
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
    given(userRepository.existsByEmail(request.email())).willReturn(false);
    given(userMapper.toEntity(request)).willReturn(userToSave);
    given(userRepository.save(userToSave)).willReturn(savedUser);
    given(userMapper.toResponse(savedUser)).willReturn(response);

    // when
    userService.register(request);

    // then
    verify(userMapper).toEntity(request);
    verify(userMapper).toResponse(savedUser);
  }
}