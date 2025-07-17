package org.project.monewping.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.user.domain.User;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.hibernate.exception.ConstraintViolationException;

/**
 * UserRepository의 데이터 접근 계층 테스트
 *
 * <p>
 * 
 * @DataJpaTest를 사용하여 JPA 관련 컴포넌트만 로드하고
 *               인메모리 H2 데이터베이스를 사용하여 Repository 계층을 테스트합니다.
 *               </p>
 */
@DataJpaTest
@EntityScan(basePackages = "org.project.monewping.domain.user.domain")
@EnableJpaRepositories(basePackages = "org.project.monewping.domain.user.repository")
@Import(JpaAuditingConfig.class)
@EnableJpaAuditing
@DisplayName("UserRepository 슬라이스 테스트")
class UserRepositorySliceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장 및 조회가 정상적으로 동작한다")
    void testSaveAndFindById() {
        // given
        User user = createTestUser("test@example.com", "testuser", "password123");

        // when
        User savedUser = userRepository.save(user);
        entityManager.flush(); // 즉시 DB에 반영
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get())
                .extracting("email", "nickname", "password")
                .containsExactly("test@example.com", "testuser", "password123");
        assertThat(foundUser.get().getId()).isNotNull();
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
        assertThat(foundUser.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 사용자 존재 여부를 확인할 수 있다")
    void testExistsByEmail() {
        // given
        User user = createTestUser("exists@example.com", "existsuser", "password123");
        userRepository.save(user);
        entityManager.flush();

        // when & then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void testFindByEmail() {
        // given
        User user = createTestUser("find@example.com", "finduser", "password123");
        userRepository.save(user);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findByEmail("find@example.com");
        Optional<User> notFoundUser = userRepository.findByEmail("notfound@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get())
                .extracting("email", "nickname", "password")
                .containsExactly("find@example.com", "finduser", "password123");

        assertThat(notFoundUser).isEmpty();
    }

    @Test
    @DisplayName("동일한 이메일로 중복 저장 시 제약조건 위반 예외가 발생한다")
    void testUniqueEmailConstraint() {
        // given
        User user1 = createTestUser("duplicate@example.com", "user1", "password1");
        User user2 = createTestUser("duplicate@example.com", "user2", "password2");

        userRepository.save(user1);
        entityManager.flush();

        // when & then
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush(); // 실제 DB 제약조건 확인을 위해 flush 필요
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("모든 사용자 조회가 정상적으로 동작한다")
    void testFindAll() {
        // given
        User user1 = createTestUser("user1@example.com", "user1", "password1");
        User user2 = createTestUser("user2@example.com", "user2", "password2");

        userRepository.save(user1);
        userRepository.save(user2);
        entityManager.flush();

        // when
        var allUsers = userRepository.findAll();

        // then
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers)
                .extracting("email")
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    /**
     * 테스트용 User 객체를 생성하는 헬퍼 메서드
     */
    private User createTestUser(String email, String nickname, String password) {
        Instant now = Instant.now();
        return User.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
