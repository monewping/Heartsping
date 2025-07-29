package org.project.monewping.domain.useractivity.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;
import org.project.monewping.domain.useractivity.exception.UserActivityNotFoundException;
import org.project.monewping.domain.useractivity.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UserActivityController 웹 레이어 슬라이스 테스트
 * 
 * <p>
 * @WebMvcTest를 사용하여 컨트롤러 레이어만 로드하고 
 * 실제 HTTP 요청/응답을 테스트합니다.
 * </p>
 */
@WebMvcTest(UserActivityController.class)
@DisplayName("UserActivityController 슬라이스 테스트")
class UserActivityControllerSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserActivityService userActivityService;

    private UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private Instant testInstant = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    @DisplayName("GET /api/user-activities/{userId} - 사용자 활동 조회 성공")
    void getUserActivity_Success() throws Exception {
        // given
        UserActivityDto.SubscriptionDto subscription = UserActivityDto.SubscriptionDto.builder()
                .id(UUID.randomUUID())
                .interestId(UUID.randomUUID())
                .interestName("테스트관심사")
                .interestKeywords(Arrays.asList("키워드1", "키워드2"))
                .interestSubscriberCount(100L)
                .createdAt(testInstant)
                .build();

        UserActivityDto.CommentDto comment = UserActivityDto.CommentDto.builder()
                .id(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .articleTitle("테스트기사")
                .userId(testUserId)
                .userNickname("테스트유저")
                .content("테스트댓글")
                .likeCount(5L)
                .createdAt(testInstant)
                .build();

        UserActivityDto expected = UserActivityDto.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("테스트유저")
                .createdAt(testInstant)
                .subscriptions(Arrays.asList(subscription))
                .comments(Arrays.asList(comment))
                .commentLikes(Collections.emptyList())
                .articleViews(Collections.emptyList())
                .updatedAt(testInstant)
                .build();

        given(userActivityService.getUserActivity(testUserId)).willReturn(expected);

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.subscriptions[0].interestName").value("테스트관심사"))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments[0].articleTitle").value("테스트기사"))
                .andExpect(jsonPath("$.comments[0].content").value("테스트댓글"));
    }

    @Test
    @DisplayName("GET /api/user-activities/{userId} - 사용자 활동 조회 실패 (존재하지 않음)")
    void getUserActivity_NotFound() throws Exception {
        // given
        given(userActivityService.getUserActivity(testUserId))
                .willThrow(new UserActivityNotFoundException("사용자 활동을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // UserActivityNotFoundException은 500으로 처리됨
    }

    @Test
    @DisplayName("GET /api/user-activities/{userId} - 빈 활동 데이터 조회 성공")
    void getUserActivity_EmptyData() throws Exception {
        // given
        UserActivityDto expected = UserActivityDto.builder()
                .id(testUserId)
                .email("empty@example.com")
                .nickname("빈활동유저")
                .createdAt(testInstant)
                .subscriptions(Collections.emptyList())
                .comments(Collections.emptyList())
                .commentLikes(Collections.emptyList())
                .articleViews(Collections.emptyList())
                .updatedAt(testInstant)
                .build();

        given(userActivityService.getUserActivity(testUserId)).willReturn(expected);

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("empty@example.com"))
                .andExpect(jsonPath("$.nickname").value("빈활동유저"))
                .andExpect(jsonPath("$.subscriptions").isEmpty())
                .andExpect(jsonPath("$.comments").isEmpty())
                .andExpect(jsonPath("$.commentLikes").isEmpty())
                .andExpect(jsonPath("$.articleViews").isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/user-activities/{userId} - 사용자 활동 삭제 성공")
    void deleteUserActivity_Success() throws Exception {
        // given
        doNothing().when(userActivityService).deleteUserActivity(testUserId);

        // when & then
        mockMvc.perform(delete("/api/user-activities/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/user-activities/{userId} - 사용자 활동 삭제 실패 (존재하지 않음)")
    void deleteUserActivity_NotFound() throws Exception {
        // given
        doThrow(new UserActivityNotFoundException("사용자 활동을 찾을 수 없습니다."))
                .when(userActivityService).deleteUserActivity(testUserId);

        // when & then
        mockMvc.perform(delete("/api/user-activities/{userId}", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // UserActivityNotFoundException은 500으로 처리됨
    }

    @Test
    @DisplayName("GET /api/user-activities/{userId} - 잘못된 UUID 형식")
    void getUserActivity_InvalidUuid() throws Exception {
        // given
        String invalidUuid = "invalid-uuid";

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/user-activities/{userId} - 잘못된 UUID 형식")
    void deleteUserActivity_InvalidUuid() throws Exception {
        // given
        String invalidUuid = "invalid-uuid";

        // when & then
        mockMvc.perform(delete("/api/user-activities/{userId}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/user-activities/{userId} - Accept 헤더 테스트")
    void getUserActivity_AcceptHeader() throws Exception {
        // given
        UserActivityDto expected = UserActivityDto.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("테스트유저")
                .createdAt(testInstant)
                .subscriptions(Collections.emptyList())
                .comments(Collections.emptyList())
                .commentLikes(Collections.emptyList())
                .articleViews(Collections.emptyList())
                .updatedAt(testInstant)
                .build();

        given(userActivityService.getUserActivity(testUserId)).willReturn(expected);

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", testUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
} 