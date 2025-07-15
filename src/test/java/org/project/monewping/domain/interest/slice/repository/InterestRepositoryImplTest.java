package org.project.monewping.domain.interest.slice.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.request.CursorPageRequestSearchInterestDto;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.mapper.InterestMapper;
import org.project.monewping.domain.interest.mapper.InterestMapperImpl;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.global.config.JpaAuditingConfig;
import org.project.monewping.global.config.QuerydslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        InterestMapperImpl.class})
@TestPropertySource(properties = "auditing.enabled=true")
class InterestRepositoryImplTest {

    @Autowired
    InterestRepository interestRepository;

    @Test
    @DisplayName("관심사 이름으로 검색하면 해당 관심사만 조회된다")
    void should_returnInterest_when_searchByName() {
        // Given
        interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        interestRepository.save(Interest.builder().name("야구").subscriberCount(0L).build());
        interestRepository.save(Interest.builder().name("농구").subscriberCount(0L).build());
        var request = new CursorPageRequestSearchInterestDto("축구", "name", "ASC", null, null, 10);
        // When
        var result = interestRepository.searchWithCursor(request, "test-user");
        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("축구");
    }

    @Test
    @DisplayName("키워드로 검색하면 해당 키워드를 가진 관심사들만 조회된다")
    void should_returnInterests_when_searchByKeyword() {
        // Given
        Interest soccer = Interest.builder().name("축구").subscriberCount(0L).build();
        Interest baseball = Interest.builder().name("야구").subscriberCount(0L).build();
        Interest basketball = Interest.builder().name("농구").subscriberCount(0L).build();
        soccer.addKeyword(Keyword.builder().name("공").build());
        baseball.addKeyword(Keyword.builder().name("방망이").build());
        basketball.addKeyword(Keyword.builder().name("공").build());
        interestRepository.save(soccer);
        interestRepository.save(baseball);
        interestRepository.save(basketball);
        var request = new CursorPageRequestSearchInterestDto("공", "name", "ASC", null, null, 10);
        // When
        var result = interestRepository.searchWithCursor(request, "test-user");
        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).extracting("name").containsExactlyInAnyOrder("축구", "농구");
    }

    @Test
    @DisplayName("존재하지 않는 관심사로 검색하면 빈 결과가 반환된다")
    void should_returnEmpty_when_searchByNonExist() {
        // Given
        interestRepository.save(Interest.builder().name("축구").subscriberCount(0L).build());
        var request = new CursorPageRequestSearchInterestDto("없는관심사", "name", "ASC", null, null, 10);
        // When
        var result = interestRepository.searchWithCursor(request, "test-user");
        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
    }
} 