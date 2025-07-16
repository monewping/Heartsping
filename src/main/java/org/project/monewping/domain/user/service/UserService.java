package org.project.monewping.domain.user.service;

import org.project.monewping.domain.user.domain.User;
import org.project.monewping.domain.user.dto.request.LoginRequest;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.response.LoginResponse;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.mapper.UserMapper;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.global.exception.EmailAlreadyExistsException;
import org.project.monewping.global.exception.LoginFailedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 회원가입을 처리합니다.
     * 
     * <p>
     * 새로운 사용자를 등록하는 과정에서 이메일 중복 검사를 수행하고,
     * 유효한 사용자 정보를 데이터베이스에 저장합니다.
     * </p>
     * 
     * <p>
     * 처리 과정:
     * </p>
     * <ol>
     * <li>이메일 중복 검사 수행</li>
     * <li>사용자 엔티티 생성</li>
     * <li>비밀번호 암호화</li>
     * <li>데이터베이스 저장</li>
     * <li>응답 객체 변환 및 반환</li>
     * </ol>
     * 
     * @param request 회원가입 요청 정보 (이메일, 닉네임, 비밀번호)
     * @return 회원가입 완료된 사용자 정보
     * @throws EmailAlreadyExistsException 이메일이 이미 존재하는 경우
     * @throws IllegalArgumentException    요청 정보가 유효하지 않은 경우
     */
    public UserRegisterResponse register(UserRegisterRequest request) {
        validateEmailNotExists(request.email());
        User user = userMapper.toEntity(request);
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());
        user = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .password(encodedPassword)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /**
     * 사용자 로그인을 처리합니다.
     * 
     * <p>
     * 이메일과 비밀번호를 검증하여 사용자 인증을 수행합니다.
     * </p>
     * 
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return 로그인 성공한 사용자 정보
     * @throws LoginFailedException 이메일 또는 비밀번호가 일치하지 않는 경우
     */
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: 이메일={}", request.email());

        try {
            User user = findUserByEmail(request.email());
            validatePassword(request.password(), user.getPassword());

            LoginResponse response = userMapper.toLoginResponse(user);
            log.info("로그인 성공: 사용자 ID={}, 이메일={}", user.getId(), user.getEmail());

            return response;
        } catch (LoginFailedException e) {
            log.warn("로그인 실패: 이메일={}, 사유={}", request.email(), e.getMessage());
            throw e;
        }
    }

    /**
     * 이메일로 사용자를 조회합니다.
     * 
     * @param email 조회할 사용자의 이메일
     * @return 사용자 엔티티
     * @throws LoginFailedException 해당 이메일의 사용자가 존재하지 않는 경우
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    /**
     * 비밀번호를 검증합니다.
     * 
     * @param rawPassword     입력된 원본 비밀번호
     * @param encodedPassword 데이터베이스에 저장된 암호화된 비밀번호
     * @throws LoginFailedException 비밀번호가 일치하지 않는 경우
     */
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new LoginFailedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 이메일 중복 검사를 수행합니다.
     * 
     * <p>
     * 회원가입 시 동일한 이메일을 가진 사용자가 이미 존재하는지 확인합니다.
     * </p>
     * 
     * @param email 검사할 이메일 주소
     * @throws EmailAlreadyExistsException 이메일이 이미 존재하는 경우
     */
    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        }
    }
}