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
 * 사용자(User) 엔티티입니다.
 *
 * <p>이메일, 닉네임, 비밀번호 등 사용자 정보를 저장합니다.</p>
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

  @Column(name = "password", nullable = false)
  private String password;

  /**
   * 닉네임을 변경합니다.
   * @param nickname 변경할 닉네임
   */
  public void updateNickname(String nickname) {
    this.nickname = nickname;
  }
}
