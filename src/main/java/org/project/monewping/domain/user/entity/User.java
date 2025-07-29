package org.project.monewping.domain.user.entity;

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

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 사용자를 논리적으로 삭제합니다.
     * 
     * <p>
     * 실제 데이터는 유지하되 삭제 표시를 합니다.
     * </p>
     */
    public void delete() {
        this.isDeleted = true;
    }

    /**
     * 사용자의 논리 삭제를 취소합니다.
     * 
     * <p>
     * 삭제 표시를 해제하고 정상 상태로 복구합니다.
     * </p>
     */
    public void restore() {
        this.isDeleted = false;
    }

    /**
     * 사용자가 삭제되었는지 확인합니다.
     * 
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.isDeleted);
    }
}