package org.project.monewping.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정 클래스
 * 
 * <p>
 * @CreatedDate, @LastModifiedDate 등의 JPA Auditing 기능을 활성화합니다.
 * 메인 애플리케이션 클래스에서 분리하여 설정 관리를 용이하게 합니다.
 * </p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
} 