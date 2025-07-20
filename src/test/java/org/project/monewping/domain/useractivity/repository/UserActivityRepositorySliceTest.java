package org.project.monewping.domain.useractivity.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.useractivity.document.UserActivityDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class UserActivityRepositorySliceTest {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Test
    @DisplayName("사용자 활동 내역 저장 및 조회")
    void saveAndFindByUserId() {
        // given
        UUID userId = UUID.randomUUID();
        UserActivityDocument.UserInfo userInfo = UserActivityDocument.UserInfo.builder()
                .id(userId)
                .email("repo@test.com")
                .nickname("레포테스터")
                .createdAt(Instant.now())
                .build();
        UserActivityDocument document = UserActivityDocument.builder()
                .userId(userId)
                .user(userInfo)
                .updatedAt(Instant.now())
                .build();

        // when
        userActivityRepository.save(document);
        Optional<UserActivityDocument> found = userActivityRepository.findByUserId(userId);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getUser().getEmail()).isEqualTo("repo@test.com");
    }

    @Test
    @DisplayName("존재 여부 확인")
    void existsByUserId() {
        // given
        UUID userId = UUID.randomUUID();
        UserActivityDocument document = UserActivityDocument.builder()
                .userId(userId)
                .user(UserActivityDocument.UserInfo.builder().id(userId).email("a@a.com").nickname("a")
                        .createdAt(Instant.now()).build())
                .updatedAt(Instant.now())
                .build();
        userActivityRepository.save(document);

        // when & then
        assertThat(userActivityRepository.existsByUserId(userId)).isTrue();
        assertThat(userActivityRepository.existsByUserId(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("삭제 동작 확인")
    void deleteByUserId() {
        // given
        UUID userId = UUID.randomUUID();
        UserActivityDocument document = UserActivityDocument.builder()
                .userId(userId)
                .user(UserActivityDocument.UserInfo.builder().id(userId).email("b@b.com").nickname("b")
                        .createdAt(Instant.now()).build())
                .updatedAt(Instant.now())
                .build();
        userActivityRepository.save(document);

        // when
        userActivityRepository.deleteByUserId(userId);

        // then
        assertThat(userActivityRepository.findByUserId(userId)).isNotPresent();
    }
}