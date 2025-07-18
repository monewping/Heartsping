package org.project.monewping.domain.interest.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Interest 엔티티 통합 테스트")
class InterestEntityTest {

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
        String name = "테스트 관심사";
        Long subscriberCount = 10L;
        Interest interest = Interest.builder().name(name).build();
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword(interest, "키워드1"));
        keywords.add(new Keyword(interest, "키워드2"));
        
        // when
        Interest testInterest = new Interest(name, subscriberCount, keywords);
        
        // then
        assertThat(testInterest.getName()).isEqualTo(name);
        assertThat(testInterest.getSubscriberCount()).isEqualTo(subscriberCount);
        assertThat(testInterest.getKeywords()).isEqualTo(keywords);
    }

    @Test
    @DisplayName("Interest 생성자 테스트 - keywords가 null인 경우")
    void testConstructorWithNullKeywords() {
        // given
        String name = "테스트 관심사";
        Long subscriberCount = 5L;
        
        // when
        Interest interest = new Interest(name, subscriberCount, null);
        
        // then
        assertThat(interest.getName()).isEqualTo(name);
        assertThat(interest.getSubscriberCount()).isEqualTo(subscriberCount);
        assertThat(interest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Interest Builder 패턴 테스트")
    void testBuilder() {
        // given
        String name = "빌더 테스트 관심사";
        Long subscriberCount = 20L;
        Interest interest = Interest.builder().name(name).build();
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword(interest, "빌더 키워드1"));
        keywords.add(new Keyword(interest, "빌더 키워드2"));
        
        // when
        Interest testInterest = Interest.builder()
                .name(name)
                .subscriberCount(subscriberCount)
                .keywords(keywords)
                .build();
        
        // then
        assertThat(testInterest.getName()).isEqualTo(name);
        assertThat(testInterest.getSubscriberCount()).isEqualTo(subscriberCount);
        assertThat(testInterest.getKeywords()).isEqualTo(keywords);
    }

    @Test
    @DisplayName("Interest 엔티티 생성 및 저장 테스트")
    void testEntityCreationAndPersistence() {
        // given
        String name = "저장 테스트 관심사";
        Long subscriberCount = 15L;
        Interest interest = Interest.builder().name(name).build();
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword(interest, "저장 키워드1"));
        keywords.add(new Keyword(interest, "저장 키워드2"));
        keywords.add(new Keyword(interest, "저장 키워드3"));
        
        // when
        Interest testInterest = Interest.builder()
                .name(name)
                .subscriberCount(subscriberCount)
                .keywords(keywords)
                .build();
        
        // then
        assertThat(testInterest).isNotNull();
        assertThat(testInterest.getName()).isEqualTo(name);
        assertThat(testInterest.getSubscriberCount()).isEqualTo(subscriberCount);
        assertThat(testInterest.getKeywords()).isEqualTo(keywords);
    }

    @Test
    @DisplayName("Interest 전체 필드 통합 테스트")
    void testAllFieldsIntegration() {
        // given
        String name1 = "관심사1";
        String name2 = "관심사2";
        Long subscriberCount1 = 10L;
        Long subscriberCount2 = 20L;
        Interest interest1 = Interest.builder().name(name1).build();
        Interest interest2 = Interest.builder().name(name2).build();
        List<Keyword> keywords1 = new ArrayList<>();
        keywords1.add(new Keyword(interest1, "키워드1"));
        List<Keyword> keywords2 = new ArrayList<>();
        keywords2.add(new Keyword(interest2, "키워드2"));
        keywords2.add(new Keyword(interest2, "키워드3"));
        
        // when
        Interest testInterest1 = Interest.builder()
                .name(name1)
                .subscriberCount(subscriberCount1)
                .keywords(keywords1)
                .build();
        
        Interest testInterest2 = Interest.builder()
                .name(name2)
                .subscriberCount(subscriberCount2)
                .keywords(keywords2)
                .build();
        
        // then
        assertThat(testInterest1).isNotEqualTo(testInterest2);
        assertThat(testInterest1.getName()).isNotEqualTo(testInterest2.getName());
        assertThat(testInterest1.getSubscriberCount()).isNotEqualTo(testInterest2.getSubscriberCount());
        assertThat(testInterest1.getKeywords()).isNotEqualTo(testInterest2.getKeywords());
    }

    @Test
    @DisplayName("기본 구독자 수 테스트")
    void testDefaultSubscriberCount() {
        // given & when
        Interest interest = Interest.builder()
                .name("기본 구독자 수 테스트")
                .build();
        
        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("기본 키워드 리스트 테스트")
    void testDefaultKeywordsList() {
        // given & when
        Interest interest = Interest.builder()
                .name("기본 키워드 테스트")
                .build();
        
        // then
        assertThat(interest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("구독자 수 증가 테스트")
    void testIncreaseSubscriberCount() {
        // given
        Interest interest = Interest.builder()
                .name("구독자 수 증가 테스트")
                .subscriberCount(5L)
                .build();
        
        // when
        interest.increaseSubscriber();
        
        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(6L);
    }

    @Test
    @DisplayName("구독자 수 감소 테스트 - 0보다 클 때")
    void testDecreaseSubscriberCountWhenGreaterThanZero() {
        // given
        Interest interest = Interest.builder()
                .name("구독자 수 감소 테스트")
                .subscriberCount(5L)
                .build();
        
        // when
        interest.decreaseSubscriber();
        
        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(4L);
    }

    @Test
    @DisplayName("구독자 수 감소 테스트 - 0일 때")
    void testDecreaseSubscriberCountWhenZero() {
        // given
        Interest interest = Interest.builder()
                .name("구독자 수 감소 테스트")
                .subscriberCount(0L)
                .build();
        
        // when
        interest.decreaseSubscriber();
        
        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("키워드 추가 테스트")
    void testAddKeyword() {
        // given
        Interest interest = Interest.builder()
                .name("키워드 추가 테스트")
                .build();
        Keyword newKeyword = new Keyword(interest, "새로운 키워드");
        
        // when
        interest.addKeyword(newKeyword);
        
        // then
        assertThat(interest.getKeywords()).contains(newKeyword);
        assertThat(interest.getKeywords()).hasSize(1);
    }

    @Test
    @DisplayName("여러 키워드 추가 테스트")
    void testAddMultipleKeywords() {
        // given
        Interest interest = Interest.builder()
                .name("여러 키워드 추가 테스트")
                .build();
        Keyword keyword1 = new Keyword(interest, "키워드1");
        Keyword keyword2 = new Keyword(interest, "키워드2");
        Keyword keyword3 = new Keyword(interest, "키워드3");
        
        // when
        interest.addKeyword(keyword1);
        interest.addKeyword(keyword2);
        interest.addKeyword(keyword3);
        
        // then
        assertThat(interest.getKeywords()).contains(keyword1, keyword2, keyword3);
        assertThat(interest.getKeywords()).hasSize(3);
    }

    @Test
    @DisplayName("키워드 제거 테스트")
    void testRemoveKeyword() {
        // given
        Interest interest = Interest.builder()
                .name("키워드 제거 테스트")
                .build();
        Keyword keyword1 = new Keyword(interest, "키워드1");
        Keyword keyword2 = new Keyword(interest, "키워드2");
        
        // when
        interest.addKeyword(keyword1);
        interest.addKeyword(keyword2);
        interest.removeKeyword(keyword1);
        
        // then
        assertThat(interest.getKeywords()).doesNotContain(keyword1);
        assertThat(interest.getKeywords()).contains(keyword2);
        assertThat(interest.getKeywords()).hasSize(1);
    }

    @Test
    @DisplayName("Interest 여러 관심사 생성 테스트")
    void testMultipleInterests() {
        // given & when
        Interest[] interests = new Interest[10];
        for (int i = 0; i < 10; i++) {
            interests[i] = Interest.builder()
                    .name("관심사" + (i + 1))
                    .subscriberCount((long) (i + 1) * 10)
                    .build();
        }
        
        // then
        for (int i = 0; i < 10; i++) {
            assertThat(interests[i].getName()).isEqualTo("관심사" + (i + 1));
            assertThat(interests[i].getSubscriberCount()).isEqualTo((long) (i + 1) * 10);
        }
    }

    @Test
    @DisplayName("Interest 구독자 수 연속 증가 테스트")
    void testConsecutiveSubscriberIncrease() {
        // given
        Interest interest = Interest.builder()
                .name("연속 증가 테스트")
                .subscriberCount(0L)
                .build();
        
        // when
        for (int i = 0; i < 5; i++) {
            interest.increaseSubscriber();
        }
        
        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Interest 구독자 수 연속 감소 테스트")
    void testConsecutiveSubscriberDecrease() {
        // given
        Interest interest = Interest.builder()
                .name("연속 감소 테스트")
                .subscriberCount(10L)
                .build();
        
        // when
        for (int i = 0; i < 3; i++) {
            interest.decreaseSubscriber();
        }
        
        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(7L);
    }

    @Test
    @DisplayName("Interest 구독자 수 0 이하로 감소 방지 테스트")
    void testSubscriberCountNotGoBelowZero() {
        // given
        Interest interest = Interest.builder()
                .name("0 이하 방지 테스트")
                .subscriberCount(2L)
                .build();
        
        // when
        interest.decreaseSubscriber(); // 2 -> 1
        interest.decreaseSubscriber(); // 1 -> 0
        interest.decreaseSubscriber(); // 0 -> 0 (변화 없음)
        interest.decreaseSubscriber(); // 0 -> 0 (변화 없음)
        
        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Interest 한글 이름 테스트")
    void testKoreanName() {
        // given
        String koreanName = "한글 관심사 이름";
        
        // when
        Interest interest = Interest.builder()
                .name(koreanName)
                .build();
        
        // then
        assertThat(interest.getName()).isEqualTo(koreanName);
    }

    @Test
    @DisplayName("Interest 영어 이름 테스트")
    void testEnglishName() {
        // given
        String englishName = "English Interest Name";
        
        // when
        Interest interest = Interest.builder()
                .name(englishName)
                .build();
        
        // then
        assertThat(interest.getName()).isEqualTo(englishName);
    }

    @Test
    @DisplayName("Interest 특수 문자 이름 테스트")
    void testSpecialCharacterName() {
        // given
        String specialName = "특수문자 관심사!@#$%^&*()";
        
        // when
        Interest interest = Interest.builder()
                .name(specialName)
                .build();
        
        // then
        assertThat(interest.getName()).isEqualTo(specialName);
    }

    @Test
    @DisplayName("Interest 긴 이름 테스트")
    void testLongName() {
        // given
        String longName = "이것은 매우 긴 관심사 이름입니다. ".repeat(10);
        
        // when
        Interest interest = Interest.builder()
                .name(longName)
                .build();
        
        // then
        assertThat(interest.getName()).isEqualTo(longName);
    }

    @Test
    @DisplayName("Interest 빈 키워드 리스트 테스트")
    void testEmptyKeywordsList() {
        // given
        Interest interest = Interest.builder()
                .name("빈 키워드 테스트")
                .keywords(new ArrayList<>())
                .build();
        
        // when
        Keyword keyword = new Keyword(interest, "테스트 키워드");
        interest.addKeyword(keyword);
        interest.removeKeyword(keyword);
        
        // then
        assertThat(interest.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Interest toString 메서드 테스트")
    void testToString() {
        // given
        Interest interest = Interest.builder()
                .name("toString 테스트")
                .subscriberCount(100L)
                .build();
        
        // when
        String result = interest.toString();
        
        // then
        assertThat(result).isNotNull();
        // 기본 Object.toString()은 클래스명과 해시코드를 포함하므로 이를 확인
        assertThat(result).contains("Interest");
        assertThat(result).contains("@");
    }
} 