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
    UserRegisterRequest request = new UserRegisterRequest(
        "integration@example.com",
        "integrationuser",
        "password123");

    // when & then - HTTP 요청/응답 검증
    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("integration@example.com"))
        .andExpect(jsonPath("$.nickname").value("integrationuser"))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.password").doesNotExist()); // 비밀번호는 응답에 포함되지 않아야 함

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
    UserRegisterRequest firstRequest = new UserRegisterRequest(
        "duplicate@example.com",
        "firstuser",
        "password123");

    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(firstRequest)))
        .andExpect(status().isCreated());

    // when - 같은 이메일로 두 번째 사용자 등록 시도
    UserRegisterRequest duplicateRequest = new UserRegisterRequest(
        "duplicate@example.com",
        "seconduser",
        "password456");

    // then - HTTP 응답 검증
    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(duplicateRequest)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));

    // then - 데이터베이스에 하나의 사용자만 존재하는지 검증
    long userCount = userRepository.count();
    assertThat(userCount).isEqualTo(1);

    Optional<User> savedUser = userRepository.findByEmail("duplicate@example.com");
    assertThat(savedUser).isPresent();
    assertThat(savedUser.get().getNickname()).isEqualTo("firstuser"); // 첫 번째 사용자만 저장됨
  }

  @Test
  @DisplayName("유효성 검사 실패 시 400 Bad Request를 반환하고 데이터베이스에 저장되지 않는다")
  void testValidationFailureIntegration() throws Exception {
    // given - 유효하지 않은 요청 (이메일 형식 오류)
    UserRegisterRequest invalidRequest = new UserRegisterRequest(
        "invalid-email",
        "testuser",
        "password123");

    long initialUserCount = userRepository.count();

    // when & then - HTTP 응답 검증
    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("유효성 검사 실패"));

    // then - 데이터베이스에 저장되지 않았는지 검증
    long finalUserCount = userRepository.count();
    assertThat(finalUserCount).isEqualTo(initialUserCount);
  }

  @Test
  @DisplayName("비밀번호가 8자 미만일 때 유효성 검사 실패")
  void testPasswordTooShortValidationIntegration() throws Exception {
    // given
    UserRegisterRequest request = new UserRegisterRequest(
        "short@example.com",
        "testuser",
        "short");

    long initialUserCount = userRepository.count();

    // when & then
    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    // 데이터베이스에 저장되지 않았는지 확인
    long finalUserCount = userRepository.count();
    assertThat(finalUserCount).isEqualTo(initialUserCount);
  }

  @Test
  @DisplayName("닉네임이 비어있을 때 유효성 검사 실패")
  void testEmptyNicknameValidationIntegration() throws Exception {
    // given
    UserRegisterRequest request = new UserRegisterRequest(
        "empty@example.com",
        "",
        "password123");

    long initialUserCount = userRepository.count();

    // when & then
    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    // 데이터베이스에 저장되지 않았는지 확인
    long finalUserCount = userRepository.count();
    assertThat(finalUserCount).isEqualTo(initialUserCount);
  }

  @Test
  @DisplayName("여러 사용자 연속 등록이 정상적으로 동작한다")
  void testMultipleUserRegistrationIntegration() throws Exception {
    // given
    UserRegisterRequest user1 = new UserRegisterRequest(
        "user1@example.com",
        "user1",
        "password123");
    UserRegisterRequest user2 = new UserRegisterRequest(
        "user2@example.com",
        "user2",
        "password456");
    UserRegisterRequest user3 = new UserRegisterRequest(
        "user3@example.com",
        "user3",
        "password789");

    // when - 연속으로 사용자 등록
    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(user2)))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(user3)))
        .andExpect(status().isCreated());

    // then - 모든 사용자가 데이터베이스에 저장되었는지 확인
    assertThat(userRepository.count()).isEqualTo(3);
    assertThat(userRepository.existsByEmail("user1@example.com")).isTrue();
    assertThat(userRepository.existsByEmail("user2@example.com")).isTrue();
    assertThat(userRepository.existsByEmail("user3@example.com")).isTrue();
  }
}
