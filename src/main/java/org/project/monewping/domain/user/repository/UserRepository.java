package org.project.monewping.domain.user.repository;

import org.project.monewping.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.UUID;

/**
 * 사용자 엔티티에 대한 데이터 접근 계층
 *
 * <p>
 * 사용자 정보의 생성, 조회, 수정, 삭제를 담당하는 리포지토리입니다.
 * Spring Data JPA를 사용하여 기본적인 CRUD 기능을 제공합니다.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 주어진 이메일로 사용자 존재 여부를 확인합니다.
     * 
     * <p>
     * 회원가입 시 이메일 중복 검사에 사용됩니다.
     * </p>
     * 
     * @param email 확인할 이메일 주소
     * @return 해당 이메일을 가진 사용자가 존재하면 true, 없으면 false
     */
    boolean existsByEmail(String email);

    /**
     * 주어진 이메일로 사용자를 조회합니다.
     * 
     * <p>
     * 로그인 시 사용자 인증에 사용됩니다.
     * </p>
     * 
     * @param email 조회할 이메일 주소
     * @return 해당 이메일을 가진 사용자의 Optional 객체
     */
    Optional<User> findByEmail(String email);
}
