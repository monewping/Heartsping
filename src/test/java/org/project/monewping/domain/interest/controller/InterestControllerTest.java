package org.project.monewping.domain.interest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.request.InterestUpdateRequest;
import org.project.monewping.domain.interest.dto.SubscriptionDto;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.exception.DuplicateKeywordException;
import org.project.monewping.domain.interest.exception.InterestNotFoundException;
import org.project.monewping.domain.interest.service.InterestService;
import org.project.monewping.domain.interest.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterestController.class)
@TestPropertySource(properties = "auditing.enabled=false")
@WithMockUser
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InterestService interestService;
    @MockitoBean
    private SubscriptionService subscriptionService;

    @Test
    @DisplayName("관심사 목록 조회 API를 호출하면 200 OK와 결과가 반환된다")
    void should_return200_when_findAllInterests() throws Exception {
        // Given
        InterestDto dto = InterestDto.builder()
                .id(UUID.randomUUID())
                .name("축구")
                .keywords(List.of("수비수", "공격수", "골키퍼"))
                .subscriberCount(10L)
                .subscribedByMe(false)
                .build();
        CursorPageResponseInterestDto response = new CursorPageResponseInterestDto(
                List.of(dto),
                null, null, 1, 1L, false
        );

        given(interestService.findInterestByNameAndSubcriberCountByCursor(any(), any(UUID.class)))
                .willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/interests")
                .param("orderBy", "name")
                .param("direction", "ASC")
                .param("limit", "10")
                .header("Monew-Request-User-ID", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("축구"))
                .andExpect(jsonPath("$.content[0].keywords[0]").value("수비수"))
                .andExpect(jsonPath("$.content[0].subscriberCount").value(10L))
                .andExpect(jsonPath("$.content[0].subscribedByMe").value(false));
    }

    @Test
    @DisplayName("키워드는 최소 1개 이상이어야 한다 - 빈 리스트면 400 반환")
    void should_return400_when_keywords_is_empty() throws Exception {
        String requestBody = """
            {
                \"name\": \"관심사 이름\",
                \"keywords\": []
            }
            """;

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/interests")
                        .with(csrf()) // CSRF 토큰 추가
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("키워드는 1개 이상 10개 이하로 입력해야 합니다.")));
    }

    @Test
    @DisplayName("관심사 키워드 수정 API를 호출하면 200 OK와 수정된 관심사 정보가 반환된다")
    void should_return200_when_updateInterestKeywords() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();
        InterestUpdateRequest request = new InterestUpdateRequest(List.of("새키워드1", "새키워드2"));
        InterestDto responseDto = InterestDto.builder()
                .id(interestId)
                .name("테스트 관심사")
                .keywords(List.of("새키워드1", "새키워드2"))
                .subscriberCount(5L)
                .subscribedByMe(false)
                .build();

        given(interestService.update(eq(interestId), any(InterestUpdateRequest.class)))
                .willReturn(responseDto);

        String requestBody = """
            {
                "keywords": ["새키워드1", "새키워드2"]
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(interestId.toString()))
                .andExpect(jsonPath("$.name").value("테스트 관심사"))
                .andExpect(jsonPath("$.keywords[0]").value("새키워드1"))
                .andExpect(jsonPath("$.keywords[1]").value("새키워드2"))
                .andExpect(jsonPath("$.subscriberCount").value(5L))
                .andExpect(jsonPath("$.subscribedByMe").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 관심사 ID로 수정 시 404 Not Found가 반환된다")
    void should_return404_when_updateNonExistentInterest() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        willThrow(new InterestNotFoundException(nonExistentId))
                .given(interestService).update(eq(nonExistentId), any(InterestUpdateRequest.class));

        String requestBody = """
            {
                "keywords": ["키워드1"]
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/interests/{interestId}", nonExistentId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("INTEREST_NOT_FOUND"))
                .andExpect(jsonPath("$.details").value("관심사를 찾을 수 없습니다: " + nonExistentId));
    }

    @Test
    @DisplayName("중복된 키워드로 수정 시 409 Conflict가 반환된다")
    void should_return409_when_duplicateKeywords() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();
        willThrow(new DuplicateKeywordException("키워드1"))
                .given(interestService).update(eq(interestId), any(InterestUpdateRequest.class));

        String requestBody = """
            {
                "keywords": ["키워드1", "키워드1"]
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("DUPLICATE_KEYWORD"))
                .andExpect(jsonPath("$.details").value("중복된 키워드입니다: 키워드1"));
    }

    @Test
    @DisplayName("키워드가 null인 경우 400 Bad Request가 반환된다")
    void should_return400_when_keywordsIsNull() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();
        String requestBody = """
            {
                "keywords": null
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효성 검사 실패"));
    }

    @Test
    @DisplayName("키워드가 10개를 초과하는 경우 400 Bad Request가 반환된다")
    void should_return400_when_keywordsExceedLimit() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();
        String requestBody = """
            {
                "keywords": ["키워드1", "키워드2", "키워드3", "키워드4", "키워드5", "키워드6", "키워드7", "키워드8", "키워드9", "키워드10", "키워드11"]
            }
            """;

        // When & Then
        mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효성 검사 실패"));
    }

    @Test
    @DisplayName("빈 키워드 리스트로 수정 시 400 Bad Request가 반환된다")
    void should_return400_when_emptyKeywordsList() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();
        String requestBody = """
                {
                    "keywords": []
                }
                """;

        // When & Then
        mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효성 검사 실패"));
    }

    @Test
    @DisplayName("관심사 구독 취소 API를 호출하면 200 OK와 결과가 반환된다")
    void should_return200_when_unsubscribeInterest() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SubscriptionDto response = SubscriptionDto.builder()
                .id(UUID.randomUUID())
                .interestId(interestId)
                .interestName("국가")
                .interestKeywords(List.of("대한민국", "일본"))
                .interestSubscriberCount(9L)
                .createdAt(java.time.Instant.now())
                .build();

        given(subscriptionService.unsubscribe(interestId, userId))
                .willReturn(response);

        // When & Then
        mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
                .with(csrf()) // CSRF 토큰 추가
                .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestId").value(interestId.toString()))
                .andExpect(jsonPath("$.interestName").value("국가"))
                .andExpect(jsonPath("$.interestSubscriberCount").value(9L))
                .andExpect(jsonPath("$.interestKeywords[0]").value("대한민국"))
                .andExpect(jsonPath("$.interestKeywords[1]").value("일본"));
    }
} 