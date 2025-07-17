package org.project.monewping.domain.interest.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Interest 엔티티 테스트")
class InterestTest {

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
    @DisplayName("키워드 제거 테스트")
    void testRemoveKeyword() {
        // given
        interest.addKeyword(keyword1);
        interest.addKeyword(keyword2);
        int initialSize = interest.getKeywords().size();

        // when
        interest.removeKeyword(keyword1);

        // then
        assertThat(interest.getKeywords()).hasSize(initialSize - 1);
        assertThat(interest.getKeywords()).doesNotContain(keyword1);
        assertThat(interest.getKeywords()).contains(keyword2);
        assertThat(keyword1.getInterest()).isNull();
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
    @DisplayName("Builder 패턴 테스트")
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
} 