package org.project.monewping.domain.interest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.interest.dto.InterestDto;
import org.project.monewping.domain.interest.dto.SubscriptionDto;
import org.project.monewping.domain.interest.dto.response.CursorPageResponseInterestDto;
import org.project.monewping.domain.interest.service.InterestService;
import org.project.monewping.domain.interest.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({InterestController.class, org.project.monewping.global.exception.GlobalExceptionHandler.class})
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

    @Test
    @DisplayName("관심사 삭제 API를 호출하면 204 No Content가 반환된다")
    void should_return204_when_deleteInterest() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/interests/{interestId}", interestId)
                .with(csrf()) // CSRF 토큰 추가
        )
        .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 관심사 삭제 시도 시 404 Not Found가 반환된다")
    void should_return404_when_deleteNonExistentInterest() throws Exception {
        // Given
        UUID interestId = UUID.randomUUID();

        org.mockito.BDDMockito.willThrow(new org.project.monewping.domain.interest.exception.InterestNotFoundException(interestId))
                .given(interestService).delete(interestId);

        // When & Then
        mockMvc.perform(delete("/api/interests/{interestId}", interestId)
                .with(csrf()) // CSRF 토큰 추가
        )
        .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
        .andExpect(status().isNotFound());
    }
} 