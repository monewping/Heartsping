package org.project.monewping.domain.user.service;

import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.dto.request.LoginRequest;
import org.project.monewping.domain.user.dto.request.UserRegisterRequest;
import org.project.monewping.domain.user.dto.request.UserNicknameUpdateRequest;
import org.project.monewping.domain.user.dto.response.LoginResponse;
import org.project.monewping.domain.user.dto.response.UserRegisterResponse;
import org.project.monewping.domain.user.mapper.UserMapper;
import org.project.monewping.domain.user.repository.UserRepository;
import org.project.monewping.domain.useractivity.service.UserActivityService;
import org.project.monewping.domain.user.repository.UserDeletionRepository;
import org.project.monewping.global.exception.EmailAlreadyExistsException;
import org.project.monewping.global.exception.LoginFailedException;
import org.project.monewping.domain.user.exception.UserNotFoundException;
import org.project.monewping.domain.user.exception.UserDeleteException;
import org.project.monewping.domain.user.exception.UserAlreadyDeletedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

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
    private final UserActivityService userActivityService;
    private final UserDeletionRepository userDeletionRepository;

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
     * <li>사용자 활동 내역 초기화</li>
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

        // 비밀번호는 평문으로 저장 (보안상 실제 운영에서는 암호화 필요)
        user = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .password(request.password())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);

        // 사용자 활동 내역 초기화
        try {
            userActivityService.initializeUserActivity(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                Instant.ofEpochMilli(savedUser.getCreatedAt().toEpochMilli())
            );
            log.info("사용자 활동 내역 초기화 완료: userId={}", savedUser.getId());
        } catch (Exception e) {
            log.error("사용자 활동 내역 초기화 실패: userId={}, error={}", savedUser.getId(), e.getMessage());
            // 활동 내역 초기화 실패가 회원가입 자체를 실패시키지 않도록 예외를 잡아서 로그만 남김
        }

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
     * @throws LoginFailedException 비밀번호가 일치하지 않는 경우
     */
    private void validatePassword(String rawPassword, String storedPassword) {
        if (!rawPassword.equals(storedPassword)) {
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

    /**
     * 사용자 닉네임을 업데이트합니다.
     * 
     * @param userId 업데이트할 사용자 ID
     * @param request 닉네임 업데이트 요청 정보
     * @return 업데이트된 사용자 정보
     * @throws IllegalArgumentException 존재하지 않는 사용자 ID인 경우
     */
    @Transactional
    public UserRegisterResponse updateNickname(UUID userId, UserNicknameUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));
        user.setNickname(request.nickname());
        User savedUser = userRepository.save(user);
        
        // MongoDB 사용자 활동 내역도 함께 업데이트
        try {
            userActivityService.updateUserNickname(userId, request.nickname());
            log.info("사용자 활동 내역 닉네임 업데이트 완료: userId={}, newNickname={}", userId, request.nickname());
        } catch (Exception e) {
            log.error("사용자 활동 내역 닉네임 업데이트 실패: userId={}, error={}", userId, e.getMessage());
            // MongoDB 업데이트 실패가 PostgreSQL 업데이트를 실패시키지 않도록 예외를 잡아서 로그만 남김
        }
        
        return userMapper.toResponse(savedUser);
    }

    /**
     * 사용자를 논리적으로 삭제합니다.
     * 
     * <p>
     * 사용자와 관련된 모든 데이터를 논리적으로 삭제 처리합니다.
     * 실제 데이터는 유지하되 삭제 표시를 합니다.
     * </p>
     * 
     * @param userId 삭제할 사용자 ID
     * @param requesterId 삭제 요청자 ID (권한 검사용)
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UserDeleteException 삭제 권한이 없는 경우
     * @throws UserAlreadyDeletedException 이미 삭제된 사용자인 경우
     */
    @Transactional
    public void softDelete(UUID userId, UUID requesterId) {
        log.info("사용자 논리 삭제 요청: userId={}, requesterId={}", userId, requesterId);

        // 사용자 존재 여부 및 삭제 상태 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        if (user.isDeleted()) {
            log.warn("이미 삭제된 사용자입니다: userId={}", userId);
            throw new UserAlreadyDeletedException(userId);
        }

        // 권한 검사 (본인만 삭제 가능)
        if (!userId.equals(requesterId)) {
            log.warn("사용자 삭제 권한이 없습니다: userId={}, requesterId={}", userId, requesterId);
            throw new UserDeleteException(userId);
        }

        // 사용자 논리 삭제
        user.delete();
        userRepository.save(user);

        // 사용자 활동 내역에서 댓글 좋아요 제거 (활동 내역 삭제 전에 실행)
        try {
            userActivityService.removeAllCommentLikesByUserId(userId);
            log.info("사용자 활동 내역 댓글 좋아요 제거 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 활동 내역 댓글 좋아요 제거 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 연관 데이터 논리 삭제 처리
        try {
            // 사용자 활동 내역에서 사용자만 논리 삭제 상태로 변경 (MongoDB)
            userActivityService.softDeleteUser(userId);
            log.info("사용자 활동 내역 논리 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 활동 내역 논리 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 구독 정보 삭제
        try {
            userDeletionRepository.deleteSubscriptionsByUserId(userId);
            log.info("구독 정보 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("구독 정보 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 댓글 논리 삭제
        try {
            userDeletionRepository.softDeleteCommentsByUserId(userId);
            log.info("댓글 논리 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("댓글 논리 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 댓글 좋아요 삭제
        try {
            userDeletionRepository.deleteCommentLikesByUserId(userId);
            log.info("댓글 좋아요 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("댓글 좋아요 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 알림 삭제
        try {
            userDeletionRepository.deleteNotificationsByUserId(userId);
            log.info("알림 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("알림 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        log.info("사용자 논리 삭제 완료: userId={}", userId);
    }

    /**
     * 사용자를 물리적으로 삭제합니다.
     * 
     * <p>
     * 사용자와 관련된 모든 데이터를 물리적으로 삭제합니다.
     * 이 작업은 되돌릴 수 없습니다.
     * </p>
     * 
     * @param userId 삭제할 사용자 ID
     * @param requesterId 삭제 요청자 ID (권한 검사용)
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UserDeleteException 삭제 권한이 없는 경우
     */
    @Transactional
    public void hardDelete(UUID userId, UUID requesterId) {
        log.info("사용자 물리 삭제 요청: userId={}, requesterId={}", userId, requesterId);

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        // 권한 검사 (본인만 삭제 가능)
        if (!userId.equals(requesterId)) {
            log.warn("사용자 삭제 권한이 없습니다: userId={}, requesterId={}", userId, requesterId);
            throw new UserDeleteException(userId);
        }

        // 연관 데이터 물리 삭제 처리
        try {
            // 사용자 활동 내역에서 댓글 좋아요 제거 (활동 내역 삭제 전에 실행)
            userActivityService.removeAllCommentLikesByUserId(userId);
            log.info("사용자 활동 내역 댓글 좋아요 제거 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 활동 내역 댓글 좋아요 제거 실패: userId={}, error={}", userId, e.getMessage());
        }

        try {
            // 사용자 활동 내역 삭제 (MongoDB)
            userActivityService.deleteUserActivity(userId);
            log.info("사용자 활동 내역 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("사용자 활동 내역 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 구독 정보 삭제
        try {
            userDeletionRepository.deleteSubscriptionsByUserId(userId);
            log.info("구독 정보 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("구독 정보 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 댓글 물리 삭제
        try {
            userDeletionRepository.deleteCommentsByUserId(userId);
            log.info("댓글 물리 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("댓글 물리 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 댓글 좋아요 삭제
        try {
            userDeletionRepository.deleteCommentLikesByUserId(userId);
            log.info("댓글 좋아요 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("댓글 좋아요 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 알림 삭제
        try {
            userDeletionRepository.deleteNotificationsByUserId(userId);
            log.info("알림 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("알림 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 기사 조회 기록 삭제
        try {
            userDeletionRepository.deleteArticleViewsByUserId(userId);
            log.info("기사 조회 기록 삭제 완료: userId={}", userId);
        } catch (Exception e) {
            log.error("기사 조회 기록 삭제 실패: userId={}, error={}", userId, e.getMessage());
        }

        // 사용자 물리 삭제
        userRepository.delete(user);

        log.info("사용자 물리 삭제 완료: userId={}", userId);
    }
}