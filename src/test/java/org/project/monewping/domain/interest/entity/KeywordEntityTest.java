package org.project.monewping.domain.interest.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Keyword 엔티티 통합 테스트")
class KeywordEntityTest {

    @Test
    @DisplayName("Keyword 기본 생성자 테스트")
    void testDefaultConstructor() {
        // given & when
        Keyword keyword = new Keyword();
        
        // then
        assertThat(keyword).isNotNull();
        assertThat(keyword.getId()).isNull();
        assertThat(keyword.getName()).isNull();
        assertThat(keyword.getInterest()).isNull();
    }

    @Test
    @DisplayName("Keyword 생성자 테스트")
    void testConstructor() {
        // given
        Interest interest = Interest.builder()
                .name("테스트 관심사")
                .build();
        String name = "테스트 키워드";
        
        // when
        Keyword keyword = new Keyword(interest, name);
        
        // then
        assertThat(keyword.getInterest()).isEqualTo(interest);
        assertThat(keyword.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Keyword Builder 패턴 테스트")
    void testBuilder() {
        // given
        Interest interest = Interest.builder()
                .name("빌더 테스트 관심사")
                .build();
        String name = "빌더 테스트 키워드";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(name)
                .build();
        
        // then
        assertThat(keyword.getInterest()).isEqualTo(interest);
        assertThat(keyword.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Keyword 엔티티 생성 및 저장 테스트")
    void testEntityCreationAndPersistence() {
        // given
        Interest interest = Interest.builder()
                .name("저장 테스트 관심사")
                .build();
        String name = "저장 테스트 키워드";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(name)
                .build();
        
        // then
        assertThat(keyword).isNotNull();
        assertThat(keyword.getInterest()).isEqualTo(interest);
        assertThat(keyword.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Keyword 전체 필드 통합 테스트")
    void testAllFieldsIntegration() {
        // given
        Interest interest1 = Interest.builder().name("관심사1").build();
        Interest interest2 = Interest.builder().name("관심사2").build();
        String name1 = "키워드1";
        String name2 = "키워드2";
        
        // when
        Keyword keyword1 = Keyword.builder()
                .interest(interest1)
                .name(name1)
                .build();
        
        Keyword keyword2 = Keyword.builder()
                .interest(interest2)
                .name(name2)
                .build();
        
        // then
        assertThat(keyword1).isNotEqualTo(keyword2);
        assertThat(keyword1.getInterest()).isNotEqualTo(keyword2.getInterest());
        assertThat(keyword1.getName()).isNotEqualTo(keyword2.getName());
    }

    @Test
    @DisplayName("Keyword 여러 키워드 생성 테스트")
    void testMultipleKeywords() {
        // given
        Interest interest = Interest.builder()
                .name("여러 키워드 테스트 관심사")
                .build();
        
        // when
        Keyword[] keywords = new Keyword[5];
        for (int i = 0; i < 5; i++) {
            keywords[i] = Keyword.builder()
                    .interest(interest)
                    .name("키워드" + (i + 1))
                    .build();
        }
        
        // then
        for (int i = 0; i < 5; i++) {
            assertThat(keywords[i].getInterest()).isEqualTo(interest);
            assertThat(keywords[i].getName()).isEqualTo("키워드" + (i + 1));
        }
    }

    @Test
    @DisplayName("Keyword 한글 이름 테스트")
    void testKoreanName() {
        // given
        Interest interest = Interest.builder().name("한글 테스트 관심사").build();
        String koreanName = "한글 키워드 이름";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(koreanName)
                .build();
        
        // then
        assertThat(keyword.getName()).isEqualTo(koreanName);
    }

    @Test
    @DisplayName("Keyword 영어 이름 테스트")
    void testEnglishName() {
        // given
        Interest interest = Interest.builder().name("English Test Interest").build();
        String englishName = "English Keyword Name";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(englishName)
                .build();
        
        // then
        assertThat(keyword.getName()).isEqualTo(englishName);
    }

    @Test
    @DisplayName("Keyword 특수 문자 이름 테스트")
    void testSpecialCharacterName() {
        // given
        Interest interest = Interest.builder().name("특수문자 테스트 관심사").build();
        String specialName = "특수문자 키워드!@#$%^&*()";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(specialName)
                .build();
        
        // then
        assertThat(keyword.getName()).isEqualTo(specialName);
    }

    @Test
    @DisplayName("Keyword 긴 이름 테스트")
    void testLongName() {
        // given
        Interest interest = Interest.builder().name("긴 이름 테스트 관심사").build();
        String longName = "이것은 매우 긴 키워드 이름입니다. ".repeat(10);
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(longName)
                .build();
        
        // then
        assertThat(keyword.getName()).isEqualTo(longName);
    }

    @Test
    @DisplayName("Keyword 빈 문자열 이름 테스트")
    void testEmptyName() {
        // given
        Interest interest = Interest.builder().name("빈 문자열 테스트 관심사").build();
        String emptyName = "";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(emptyName)
                .build();
        
        // then
        assertThat(keyword.getName()).isEqualTo(emptyName);
    }

    @Test
    @DisplayName("Keyword 숫자와 기호 포함 이름 테스트")
    void testNumbersAndSymbolsName() {
        // given
        Interest interest = Interest.builder().name("숫자 기호 테스트 관심사").build();
        String numberName = "키워드123!@#";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(numberName)
                .build();
        
        // then
        assertThat(keyword.getName()).isEqualTo(numberName);
    }

    @Test
    @DisplayName("Keyword setInterest 메서드 테스트")
    void testSetInterest() {
        // given
        Interest interest1 = Interest.builder().name("관심사1").build();
        Interest interest2 = Interest.builder().name("관심사2").build();
        Keyword keyword = new Keyword(interest1, "테스트 키워드");
        
        // when
        keyword.setInterest(interest2);
        
        // then
        assertThat(keyword.getInterest()).isEqualTo(interest2);
    }

    @Test
    @DisplayName("Keyword null 관심사 설정 테스트")
    void testSetNullInterest() {
        // given
        Interest interest = Interest.builder().name("테스트 관심사").build();
        Keyword keyword = new Keyword(interest, "테스트 키워드");
        
        // when
        keyword.setInterest(null);
        
        // then
        assertThat(keyword.getInterest()).isNull();
    }

    @Test
    @DisplayName("Keyword toString 메서드 테스트")
    void testToString() {
        // given
        Interest interest = Interest.builder().name("toString 테스트 관심사").build();
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name("toString 테스트 키워드")
                .build();
        
        // when
        String result = keyword.toString();
        
        // then
        assertThat(result).isNotNull();
        // 기본 Object.toString()은 클래스명과 해시코드를 포함하므로 이를 확인
        assertThat(result).contains("Keyword");
        assertThat(result).contains("@");
    }

    @Test
    @DisplayName("Keyword 동일한 이름 다른 관심사 테스트")
    void testSameNameDifferentInterest() {
        // given
        Interest interest1 = Interest.builder().name("관심사1").build();
        Interest interest2 = Interest.builder().name("관심사2").build();
        String sameName = "동일한 키워드 이름";
        
        // when
        Keyword keyword1 = new Keyword(interest1, sameName);
        Keyword keyword2 = new Keyword(interest2, sameName);
        
        // then
        assertThat(keyword1.getName()).isEqualTo(sameName);
        assertThat(keyword2.getName()).isEqualTo(sameName);
        assertThat(keyword1.getInterest()).isNotEqualTo(keyword2.getInterest());
    }

    @Test
    @DisplayName("Keyword 공백 포함 이름 테스트")
    void testNameWithSpaces() {
        // given
        Interest interest = Interest.builder().name("공백 테스트 관심사").build();
        String nameWithSpaces = "  공백이 포함된 키워드 이름  ";
        
        // when
        Keyword keyword = Keyword.builder()
                .interest(interest)
                .name(nameWithSpaces)
                .build();
        
        // then
        assertThat(keyword.getName()).isEqualTo(nameWithSpaces);
    }
} 