package org.project.monewping.domain.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.project.monewping.global.base.BaseUpdatableEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 사용자 정보를 나타내는 엔티티 클래스
 * 
 * <p>
 * 사용자의 기본 정보인 이메일, 닉네임, 비밀번호를 저장하며,
 * 생성일시와 수정일시는 BaseEntity와 BaseUpdatableEntity에서 관리됩니다.
 * </p>
 * 
 * <p>
 * 데이터베이스 테이블명: users
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseUpdatableEntity {

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "password", nullable = false, length = 100)
    private String password;
}