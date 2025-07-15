package org.project.monewping.domain.user.Integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 사용자 기능의 통합 테스트
 *
 * <p>
 * @SpringBootTest를 사용하여 전체 애플리케이션 컨텍스트를 로드하고
 * Controller → Service → Repository의 전체 플로우를 테스트합니다.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User 통합 테스트")
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("정상적인 회원가입이 성공하고 데이터베이스에 저장된다")
  void testSuccessfulUserRegistrationIntegration() throws Exception {
    // given
    UserRegisterRequest request = createUserRequest(
        "integration@example.com",
        "integrationuser", 
        "password123");

    // when & then - HTTP 요청/응답 검증
    org.springframework.test.web.servlet.ResultActions result = performUserRegistration(request);
    assertSuccessfulRegistration(result, "integration@example.com", "integrationuser");

    // then - 데이터베이스 저장 검증
    Optional<User> savedUser = userRepository.findByEmail("integration@example.com");
    assertThat(savedUser).isPresent();
    assertThat(savedUser.get())
        .extracting("email", "nickname", "password")
        .containsExactly("integration@example.com", "integrationuser", "password123");
    assertThat(savedUser.get().getId()).isNotNull();
    assertThat(savedUser.get().getCreatedAt()).isNotNull();
    assertThat(savedUser.get().getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("이메일 중복 시 409 Conflict를 반환하고 데이터베이스에 중복 저장되지 않는다")
  void testEmailDuplicationIntegration() throws Exception {
    // given - 첫 번째 사용자 등록
    UserRegisterRequest firstRequest = createUserRequest(
        "duplicate@example.com",
        "firstuser",
        "password123");

    performUserRegistration(firstRequest)
        .andExpect(status().isCreated());

    // when - 같은 이메일로 두 번째 사용자 등록 시도
    UserRegisterRequest duplicateRequest = createUserRequest(
        "duplicate@example.com",
        "seconduser",
        "password456");

    // then - HTTP 응답 검증
    org.springframework.test.web.servlet.ResultActions result = performUserRegistration(duplicateRequest);
    assertEmailDuplicationFailure(result);

    // then - 데이터베이스에 하나의 사용자만 존재하는지 검증
    assertUserCount(1);
    assertUserExists("duplicate@example.com", "firstuser"); // 첫 번째 사용자만 저장됨
  }

  @Test
  @DisplayName("유효성 검사 실패 시 400 Bad Request를 반환하고 데이터베이스에 저장되지 않는다")
  void testValidationFailureIntegration() throws Exception {
    // given - 유효하지 않은 요청 (이메일 형식 오류)
    UserRegisterRequest invalidRequest = createUserRequest(
        "invalid-email",
        "testuser",
        "password123");

    long initialUserCount = userRepository.count();

    // when & then - HTTP 응답 검증
    org.springframework.test.web.servlet.ResultActions result = performUserRegistration(invalidRequest);
    assertValidationFailure(result);

    // then - 데이터베이스에 저장되지 않았는지 검증
    assertUserCount(initialUserCount);
  }

  @Test
  @DisplayName("비밀번호가 8자 미만일 때 유효성 검사 실패")
  void testPasswordTooShortValidationIntegration() throws Exception {
    // given
    UserRegisterRequest request = createUserRequest(
        "short@example.com",
        "testuser",
        "short");

    long initialUserCount = userRepository.count();

    // when & then
    org.springframework.test.web.servlet.ResultActions result = performUserRegistration(request);
    result.andExpect(status().isBadRequest());

    // 데이터베이스에 저장되지 않았는지 확인
    assertUserCount(initialUserCount);
  }

  @Test
  @DisplayName("닉네임이 비어있을 때 유효성 검사 실패")
  void testEmptyNicknameValidationIntegration() throws Exception {
    // given
    UserRegisterRequest request = createUserRequest(
        "empty@example.com",
        "",
        "password123");

    long initialUserCount = userRepository.count();

    // when & then
    org.springframework.test.web.servlet.ResultActions result = performUserRegistration(request);
    result.andExpect(status().isBadRequest());

    // 데이터베이스에 저장되지 않았는지 확인
    assertUserCount(initialUserCount);
  }

  @Test
  @DisplayName("여러 사용자 연속 등록이 정상적으로 동작한다")
  void testMultipleUserRegistrationIntegration() throws Exception {
    // given
    UserRegisterRequest user1 = createUserRequest("user1@example.com", "user1", "password123");
    UserRegisterRequest user2 = createUserRequest("user2@example.com", "user2", "password456");
    UserRegisterRequest user3 = createUserRequest("user3@example.com", "user3", "password789");

    // when - 연속으로 사용자 등록
    performUserRegistration(user1).andExpect(status().isCreated());
    performUserRegistration(user2).andExpect(status().isCreated());
    performUserRegistration(user3).andExpect(status().isCreated());

    // then - 모든 사용자가 데이터베이스에 저장되었는지 확인
    assertUserCount(3);
    assertUserExistsByEmail("user1@example.com", true);
    assertUserExistsByEmail("user2@example.com", true);
    assertUserExistsByEmail("user3@example.com", true);
  }

  // ===== Private Helper Methods =====

  /**
   * 사용자 등록 POST 요청을 수행합니다.
   */
  private org.springframework.test.web.servlet.ResultActions performUserRegistration(UserRegisterRequest request) throws Exception {
    return mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andDo(print());
  }

  /**
   * UserRegisterRequest 객체를 생성합니다.
   */
  private UserRegisterRequest createUserRequest(String email, String nickname, String password) {
    return new UserRegisterRequest(email, nickname, password);
  }

  /**
   * 데이터베이스의 사용자 수를 검증합니다.
   */
  private void assertUserCount(long expectedCount) {
    assertThat(userRepository.count()).isEqualTo(expectedCount);
  }

  /**
   * 특정 이메일의 사용자가 존재하는지 검증합니다.
   */
  private void assertUserExists(String email, String expectedNickname) {
    Optional<User> savedUser = userRepository.findByEmail(email);
    assertThat(savedUser).isPresent();
    assertThat(savedUser.get().getNickname()).isEqualTo(expectedNickname);
  }

  /**
   * 성공적인 회원가입 응답을 검증합니다.
   */
  private void assertSuccessfulRegistration(org.springframework.test.web.servlet.ResultActions result, 
                                          String email, String nickname) throws Exception {
    result.andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.nickname").value(nickname))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  /**
   * 유효성 검사 실패 응답을 검증합니다.
   */
  private void assertValidationFailure(org.springframework.test.web.servlet.ResultActions result) throws Exception {
    result.andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("유효성 검사 실패"));
  }

  /**
   * 이메일 중복 오류 응답을 검증합니다.
   */
  private void assertEmailDuplicationFailure(org.springframework.test.web.servlet.ResultActions result) throws Exception {
    result.andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
  }

  /**
   * 사용자 존재 여부를 boolean으로 확인합니다.
   */
  private void assertUserExistsByEmail(String email, boolean shouldExist) {
    assertThat(userRepository.existsByEmail(email)).isEqualTo(shouldExist);
  }
}
