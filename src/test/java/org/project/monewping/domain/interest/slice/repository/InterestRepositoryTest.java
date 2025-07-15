package org.project.monewping.domain.interest.slice.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.mapper.InterestMapperImpl;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        InterestMapperImpl.class})
@TestPropertySource(properties = "auditing.enabled=true")
class InterestRepositoryTest {

    @Autowired
    InterestRepository interestRepository;

    @Test
    @DisplayName("레포지토리에 관심사 데이터가 잘 저장된다")
    void should_saveInterest() {
        // Given
        Interest interest = Interest.builder()
                .name("축구")
                .subscriberCount(0L)
                .build();

        // When
        Interest saved = interestRepository.save(interest);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("축구");
    }
} 