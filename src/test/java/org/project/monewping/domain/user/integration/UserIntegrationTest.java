package org.project.monewping.domain.user.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.project.monewping.domain.user.dto.request.LoginRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 사용자 기능의 통합 테스트
 *
 * <p>
 *
 * @SpringBootTest를 사용하여 전체 애플리케이션 컨텍스트를 로드하고
 *                  Controller → Service → Repository의 전체 플로우를 테스트합니다.
 *                  </p>
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
    void testSuccessfulUserRegistrationIntegration() thr
                iven
                RegisterRequest re
                "integration@example.com",
            "integrationuser",
            "password123");

        // when & then - HTTP 요청/응답 검증
        org.springframework.test.web.servlet.ResultActions result = performUserRegistration(request);
        assertSuccessfulRegistration(result, "integration@example.com", "integrationuser");

        // then - 데이터베이스 저장 검증
        Optional<User> savedUser = userRepository.findByEmail("integration@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getEmail()).isEqualTo("integration@example.com");
        assertThat(savedUser.get().getNickname()).isEqualTo("integrationuser");
        assertThat(savedUser.get().getPassword()).isNotEqualTo("password123"); // 암호화되어야 함
        assertThat(savedUser.get().getPassword()).isNotEmpty(); // 비밀번호가 저장되어야 함
        assertThat(savedUser.get().getId()).isNotNull();
        assertThat(savedUser.get().getCreatedAt()).isNotNull();
        assertThat(savedUser.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일 중복 시 409 Conflict를 반환하고 데이터베이스에 중복 저장되지 않는다")
    void testEmailDuplicationIntegration() throws Exception {
                iven - 첫 번째 사용자 등록
                RegisterRequ
                "duplicate@example.com",
            "firstuser",
            "password123");
                
        performUserRegistration(firstRequest)
            .andExpect(status().isCreated());

                hen - 같은 이메일로 두 번째 사용자 등
                RegisterReque
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
                iven - 유효하지 않은 요
                RegisterReq
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
    void testPasswordTooShortValidationIntegration() thr
                iven
                RegisterReq
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
    void testEmptyNicknameValidationIntegration() throws
                iven
                Reg
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

    // ========== 로그인 관련 통합 테스트 ==========
    // 회원가입 후 로그인 기능의 전체 플로우를 검증합니다.
    // 실제 데이터베이스와 연동하여 Controller → Service → Repository 전체 계층을 테스트합니다.

    @Test
    @DisplayName("회원가입 후 로그인이 정상적으로 성공한다")
    void testSuccessfulLoginAfterRegistrationIntegration() throw
                iven - 사용자 등록
                RegisterRequ
                "login@example.com",
            "loginuser",
            "password123");
                
        performUserRegistration(registerRequest)
            .andExpect(status().isCreated());

        // when - 로그인 시도
        LoginRequest loginRequest = new LoginRequest("login@example.com", "password123");

        // then - 로그인 성공 검증
        org.springframework.test.web.servlet.ResultActions result = performLogin(loginRequest);
        assertSuccessfulLogin(result, "login@example.com", "loginuser");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 401 Unauthorized를 반환한다")
    void testLoginWithNonExistentEmailIntegration() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");

        // when & then
        org.springframework.test.web.servlet.ResultActions result = performLogin(loginRequest);
        assertLoginFailure(result, "이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 Unauthorized를 반환한다")
    void testLoginWithWrongPasswordIntegration() throws Exceptio
                iven - 사용자 등록
                RegisterRequest 
                "wrongpass@example.com",
            "wrongpassuser",
            "password123");
                
        performUserRegistration(registerRequest)
            .andExpect(status().isCreated());

        // when - 잘못된 비밀번호로 로그인 시도
        LoginRequest loginRequest = new LoginRequest("wrongpass@example.com", "wrongpassword");

        // then - 로그인 실패 검증
        org.springframework.test.web.servlet.ResultActions result = performLogin(loginRequest);
        assertLoginFailure(result, "이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("빈 이메일로 로그인 시 400 Bad Request를 반환한다")
    void testLoginWithEmptyEmailIntegration() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("", "password123");

        // when & then
        org.springframework.test.web.servlet.ResultActions result = performLogin(loginRequest);
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 비밀번호로 로그인 시 400 Bad Request를 반환한다")
    void testLoginWithEmptyPasswordIntegration() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "");

        // when & then
        org.springframework.test.web.servlet.ResultActions result = performLogin(loginRequest);
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여러 사용자 등록 후 각각 로그인이 정상적으로 동작한다")
    void testMultipleUserLoginIntegration() throws Exception {
        // given - 여러 사용자 등록
        UserRegisterRequest user1 = createUserRequest("multi1@example.com", "multi1", "password123");
        UserRegisterRequest user2 = createUserRequest("multi2@example.com", "multi2", "password456");
        UserRegisterRequest user3 = createUserRequest("multi3@example.com", "multi3", "password789");

        performUserRegistration(user1).andExpect(status().isCreated());
        performUserRegistration(user2).andExpect(status().isCreated());
        performUserRegistration(user3).andExpect(status().isCreated());

        // when & then - 각 사용자별 로그인 성공 검증
        LoginRequest login1 = new LoginRequest("multi1@example.com", "password123");
        LoginRequest login2 = new LoginRequest("multi2@example.com", "password456");
        LoginRequest login3 = new LoginRequest("multi3@example.com", "password789");

        assertSuccessfulLogin(performLogin(login1), "multi1@example.com", "multi1");
        assertSuccessfulLogin(performLogin(login2), "multi2@example.com", "multi2");
        assertSuccessfulLogin(performLogin(login3), "multi3@example.com", "multi3");
    }

    // ===== Private Helper Methods =====

    /**
     * 
     *         
     * 
     * ate org.springfram
     * throws Exception {
     * vc.perform(post("/api/users")
     *     .contentType(MediaType.APPLICATION_JSON)
     *     .content(obj
     *  
     * 
     * 
    /**
     * 
     *         
     * 
     * ate org.springfram
     * throws Exception {
     * vc.perform(post("/api/users/login")
     *     .contentType(MediaType.APPLICATION_JSON)
     *     .content(obj
     *  
     * 
     * 
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
            
    private void assertSuccessfulRegistration(
                ng email, String nickname) throws Exception {
                lt.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICA
                .andExpect(jsonPath("$.email").value(
                .andExpect(jsonPath("$.nickname").value(nick
                .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    /**
     * 유효성 검사 실패 응답을 검증합니다.
     */
                void assertValidationFailure(org.springframework.test.web.ser
                lt.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("유효성 검사 실패"));
    }

    /**
     * 
     *         
     *  assertEmailDu
     * ws Exception {
     * lt.andExpect(status().isConflict())
     *     .andExpect(content().contentType(MediaT
     * .andExpect(jsonPath("$.status").value(409))
     *  
     * 
     * 
    /**
     * 
     *         
     *  assertSuccessfulLogin(or
     * ng email, String nickname) th
     * lt.andExpect(status().isOk())
     * .andExpect(content().contentType(Medi
     * .andExpect(jsonPath("$.email").value(
     * .andExpect(jsonPath("$.nickname").val
     *     .andExpect(jsonPath("$.id").exists())
     * .andExpect(jsonPath("$.createdAt").exists())
     *  
     * 
     * 
    /**
     * 
     *         
     *  assertLoginFa
     * ws Exception {
     * lt.andExpect(status().isUnauthorized())
     *     .andExpect(content().contentType(MediaT
     * .andExpect(jsonPath("$.status").value(401))
     *  
     * 
     * 
    /**
     * 사용자 존재 여부를 boolean으로 확인합니다.
     */
    private void assertUserExistsByEmail(String email, boolean shouldExist) {
        assertThat(userRepository.existsByEmail(email)).isEqualTo(shouldExist);
    }
}
