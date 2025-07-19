package org.project.monewping.domain.interest.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Interest 엔티티 통합 테스트")
class InterestEntityTest {

    private Interest interest;
    private Keyword keyword1;
    private Keyword keyword2;

    @BeforeEach
    void setUp() {
        interest = Interest.builder()
            .name("테스트 관심사")
            .subscriberCount(0L)
            .keywords(new ArrayList<>())
            .build();

        keyword1 = Keyword.builder()
            .name("테스트 키워드1")
            .build();

        keyword2 = Keyword.builder()
            .name("테스트 키워드2")
            .build();
    }

    @Test
    @DisplayName("Interest 기본 생성자 테스트")
    void testDefaultConstructor() {
        // given & when
        Interest interest = new Interest();
        
        // then
        assertThat(interest).isNotNull();
        assertThat(interest.getId()).isNull();
        assertThat(interest.getName()).isNull();
        assertThat(interest.getSubscriberCount()).isEqualTo(0L);
        assertThat(interest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Interest 생성자 테스트")
    void testConstructor() {
        // given
        String name = "새로운 관심사";
        Long subscriberCount = 5L;
        List<Keyword> keywords = new ArrayList<>();

        // when
        Interest newInterest = new Interest(name, subscriberCount, keywords);

        // then
        assertThat(newInterest.getName()).isEqualTo(name);
        assertThat(newInterest.getSubscriberCount()).isEqualTo(subscriberCount);
        assertThat(newInterest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Interest 생성자 테스트 - keywords가 null인 경우")
    void testConstructorWithNullKeywords() {
        // given
        String name = "새로운 관심사";
        Long subscriberCount = 5L;

        // when
        Interest newInterest = new Interest(name, subscriberCount, null);

        // then
        assertThat(newInterest.getName()).isEqualTo(name);
        assertThat(newInterest.getSubscriberCount()).isEqualTo(subscriberCount);
        assertThat(newInterest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Interest Builder 패턴 테스트")
    void testBuilder() {
        // given & when
        Interest builtInterest = Interest.builder()
            .name("빌더로 생성된 관심사")
            .subscriberCount(10L)
            .keywords(List.of(keyword1, keyword2))
            .build();

        // then
        assertThat(builtInterest.getName()).isEqualTo("빌더로 생성된 관심사");
        assertThat(builtInterest.getSubscriberCount()).isEqualTo(10L);
        assertThat(builtInterest.getKeywords()).contains(keyword1, keyword2);
    }

    @Test
    @DisplayName("기본 구독자 수 테스트")
    void testDefaultSubscriberCount() {
        // given & when
        Interest defaultInterest = Interest.builder()
            .name("기본 관심사")
            .build();

        // then
        assertThat(defaultInterest.getSubscriberCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("기본 키워드 리스트 테스트")
    void testDefaultKeywords() {
        // given & when
        Interest defaultInterest = Interest.builder()
            .name("기본 관심사")
            .build();

        // then
        assertThat(defaultInterest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("구독자 수 증가 테스트")
    void testIncreaseSubscriber() {
        // given
        Long initialCount = interest.getSubscriberCount();

        // when
        interest.increaseSubscriber();

        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("구독자 수 감소 테스트 - 0보다 클 때")
    void testDecreaseSubscriberWhenGreaterThanZero() {
        // given
        interest.increaseSubscriber();
        interest.increaseSubscriber();
        Long initialCount = interest.getSubscriberCount();

        // when
        interest.decreaseSubscriber();

        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(initialCount - 1);
    }

    @Test
    @DisplayName("구독자 수 감소 테스트 - 0일 때")
    void testDecreaseSubscriberWhenZero() {
        // given
        Long initialCount = interest.getSubscriberCount();

        // when
        interest.decreaseSubscriber();

        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("키워드 추가 테스트")
    void testAddKeyword() {
        // given
        int initialSize = interest.getKeywords().size();

        // when
        interest.addKeyword(keyword1);

        // then
        assertThat(interest.getKeywords()).hasSize(initialSize + 1);
        assertThat(interest.getKeywords()).contains(keyword1);
        assertThat(keyword1.getInterest()).isEqualTo(interest);
    }

    @Test
    @DisplayName("여러 키워드 추가 테스트")
    void testAddMultipleKeywords() {
        // given
        int initialSize = interest.getKeywords().size();

        // when
        interest.addKeyword(keyword1);
        interest.addKeyword(keyword2);

        // then
        assertThat(interest.getKeywords()).hasSize(initialSize + 2);
        assertThat(interest.getKeywords()).contains(keyword1, keyword2);
        assertThat(keyword1.getInterest()).isEqualTo(interest);
        assertThat(keyword2.getInterest()).isEqualTo(interest);
    }

    @Test
    @DisplayName("Interest 구독자 수 연속 증가 테스트")
    void testConsecutiveSubscriberIncrease() {
        // given
        Long initialCount = interest.getSubscriberCount();

        // when
        interest.increaseSubscriber();
        interest.increaseSubscriber();
        interest.increaseSubscriber();

        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(initialCount + 3);
    }

    @Test
    @DisplayName("Interest 구독자 수 연속 감소 테스트")
    void testConsecutiveSubscriberDecrease() {
        // given
        interest.increaseSubscriber();
        interest.increaseSubscriber();
        interest.increaseSubscriber();
        Long initialCount = interest.getSubscriberCount();

        // when
        interest.decreaseSubscriber();
        interest.decreaseSubscriber();

        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(initialCount - 2);
    }

    @Test
    @DisplayName("Interest 구독자 수 0 이하로 감소 방지 테스트")
    void testSubscriberCountNotGoBelowZero() {
        // given
        Long initialCount = interest.getSubscriberCount();

        // when
        interest.decreaseSubscriber();
        interest.decreaseSubscriber();
        interest.decreaseSubscriber();

        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(0L);
        assertThat(interest.getSubscriberCount()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("Interest 한글 이름 테스트")
    void testKoreanName() {
        // given
        String koreanName = "한글 관심사";

        // when
        Interest koreanInterest = Interest.builder()
                .name(koreanName)
                .build();

        // then
        assertThat(koreanInterest.getName()).isEqualTo(koreanName);
    }

    @Test
    @DisplayName("Interest 영어 이름 테스트")
    void testEnglishName() {
        // given
        String englishName = "English Interest";

        // when
        Interest englishInterest = Interest.builder()
                .name(englishName)
                .build();

        // then
        assertThat(englishInterest.getName()).isEqualTo(englishName);
    }

    @Test
    @DisplayName("Interest 특수 문자 이름 테스트")
    void testSpecialCharacterName() {
        // given
        String specialName = "관심사@#$%";

        // when
        Interest specialInterest = Interest.builder()
                .name(specialName)
                .build();

        // then
        assertThat(specialInterest.getName()).isEqualTo(specialName);
    }

    @Test
    @DisplayName("Interest 긴 이름 테스트")
    void testLongName() {
        // given
        String longName = "매우 긴 관심사 이름입니다. 이 이름은 테스트를 위해 만들어졌습니다.";

        // when
        Interest longNameInterest = Interest.builder()
                .name(longName)
                .build();

        // then
        assertThat(longNameInterest.getName()).isEqualTo(longName);
    }

    @Test
    @DisplayName("Interest 빈 키워드 리스트 테스트")
    void testEmptyKeywordsList() {
        // given
        List<Keyword> emptyKeywords = new ArrayList<>();

        // when
        Interest emptyKeywordsInterest = Interest.builder()
                .name("빈 키워드 테스트")
                .keywords(emptyKeywords)
                .build();

        // then
        assertThat(emptyKeywordsInterest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Interest toString 메서드 테스트")
    void testToString() {
        // given
        Interest testInterest = Interest.builder()
                .name("toString 테스트")
                .subscriberCount(5L)
                .build();

        // when
        String result = testInterest.toString();

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains("Interest");
    }
} 