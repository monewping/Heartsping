package org.project.monewping.domain.interest.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.InterestRegisterRequest;
import org.project.monewping.domain.interest.entity.Interest;
import org.project.monewping.domain.interest.entity.Keyword;
import org.project.monewping.domain.interest.exception.DuplicateInterestNameException;
import org.project.monewping.domain.interest.repository.InterestRepository;
import org.project.monewping.domain.interest.service.InterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InterestServiceIntegrationTest {

    @Autowired
    InterestService interestService;

    @Autowired
    InterestRepository interestRepository;

    @Test
    @DisplayName("관심사와 키워드가 정상적으로 저장된다")
    void createInterestWithKeywords() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "여행",
                List.of("해외여행", "국내여행")
        );

        // when
        var dto = interestService.create(request);

        // then
        Interest saved = interestRepository.findByName("여행").orElseThrow();
        assertThat(saved.getName()).isEqualTo("여행");
        assertThat(saved.getKeywords()).hasSize(2);
        assertThat(saved.getKeywords())
                .extracting(Keyword::getKeyword)
                .containsExactlyInAnyOrder("해외여행", "국내여행");
    }

    @Test
    @DisplayName("중복된 관심사명으로 등록 시 예외 발생")
    void duplicateInterestName() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "음악",
                List.of("K-POP")
        );
        interestService.create(request);

        // when & then
        assertThatThrownBy(() -> interestService.create(request))
                .isInstanceOf(DuplicateInterestNameException.class);
    }

    @Test
    @DisplayName("유효하지 않은 관심사명(빈 값) 등록 시 예외 발생")
    void invalidInterestName() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest(
                "",
                List.of("테스트")
        );

        // when & then
        assertThatThrownBy(() -> interestService.create(request))
                .isInstanceOf(Exception.class); // 실제로는 MethodArgumentNotValidException 등 발생
    }
}