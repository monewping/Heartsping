package org.project.monewping.domain.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link User} 엔티티의 도메인 로직을 테스트하는 클래스
 * 
 * <p>
 * User 엔티티의 생성, 필드 접근, 비즈니스 메서드 등을 테스트합니다.
 * 단위 테스트로 구성되어 있으며, 데이터베이스 연결 없이 객체 자체의 동작을 검증합니다.
 * </p>
 */
class UserTest {

    /**
     * User 객체가 빌더 패턴으로 올바르게 생성되는지 테스트합니다.
     * 
     * <p>
     * 빌더 패턴을 사용하여 User 객체를 생성하고,
     * 설정한 값들이 정확히 저장되는지 검증합니다.
     * </p>
     */
    @Test
    @DisplayName("User 객체를 빌더로 생성할 수 있다")
    void createUserWithBuilder() {
        // given & when
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .password("password123")
                .build();

        // then
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getNickname()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password123");
    }
}