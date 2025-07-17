package org.project.monewping.global.config;


import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * 
 * <p>
 * 비밀번호 인코딩과 보안 설정을 담당합니다.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 비밀번호 인코더 Bean을 정의합니다.
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인을 설정합니다.
     * 
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화 (REST API의 경우)
                .authorizeHttpRequests(authz -> authz
//                         .requestMatchers("/api/users/login", "/api/users").permitAll() // 로그인, 회원가입 허용
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll() // 정적 리소스 허용
                        .requestMatchers("/api/**").permitAll() // api 요청 전체 허용 (일시적으로 허용)
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                ).httpBasic(withDefaults());
        return http.build();
    }
}