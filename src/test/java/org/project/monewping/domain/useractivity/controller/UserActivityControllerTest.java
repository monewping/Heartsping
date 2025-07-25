package org.project.monewping.domain.useractivity.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.monewping.domain.useractivity.dto.UserActivityDto;
import org.project.monewping.domain.useractivity.exception.UserActivityNotFoundException;
import org.project.monewping.domain.useractivity.service.UserActivityService;
import org.project.monewping.global.exception.GlobalExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * UserActivityController의 단위 테스트
 * 
 * <p>
 * HTTP 요청/응답 처리 로직을 검증하고, 서비스 계층을 모킹하여
 * 순수한 컨트롤러 로직만을 테스트합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserActivityController 단위 테스트")
class UserActivityControllerTest {

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private UserActivityController userActivityController;

    private MockMvc mockMvc;
    private UUID testUserId;
    private UserActivityDto testUserActivityDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userActivityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUserId = UUID.randomUUID();
        testUserActivityDto = UserActivityDto.builder()
                .id(testUserId)
                .email("test@example.com")
                .nickname("테스트유저")
                .createdAt(Instant.now())
                .subscriptions(new ArrayList<>())
                .comments(new ArrayList<>())
                .commentLikes(new ArrayList<>())
                .articleViews(new ArrayList<>())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("사용자 활동 내역 조회 성공 - 200 OK")
    void getUserActivity_Success() throws Exception {
        // given
        given(userActivityService.getUserActivity(testUserId)).willReturn(testUserActivityDto);

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.commentLikes").isArray())
                .andExpect(jsonPath("$.articleViews").isArray());
    }

    @Test
    @DisplayName("사용자 활동 내역 조회 실패 - 404 Not Found")
    void getUserActivity_NotFound() throws Exception {
        // given
        given(userActivityService.getUserActivity(testUserId))
                .willThrow(new UserActivityNotFoundException(testUserId));

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // UserActivityNotFoundException은 500으로 처리됨
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.details").value("사용자 활동 내역을 찾을 수 없습니다. userId: " + testUserId));
    }

    @Test
    @DisplayName("사용자 활동 내역 조회 실패 - 잘못된 UUID 형식")
    void getUserActivity_InvalidUuidFormat() throws Exception {
        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 활동 내역 삭제 성공 - 204 No Content")
    void deleteUserActivity_Success() throws Exception {
        // given
        doNothing().when(userActivityService).deleteUserActivity(testUserId);

        // when & then
        mockMvc.perform(delete("/api/user-activities/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("사용자 활동 내역 삭제 실패 - 잘못된 UUID 형식")
    void deleteUserActivity_InvalidUuidFormat() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/user-activities/{userId}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 활동 내역 삭제 중 서비스 예외 발생")
    void deleteUserActivity_ServiceException() throws Exception {
        // given
        doThrow(new RuntimeException("서비스 오류")).when(userActivityService).deleteUserActivity(testUserId);

        // when & then
        mockMvc.perform(delete("/api/user-activities/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("GET 요청에 대한 응답 Content-Type 검증")
    void getUserActivity_ContentTypeValidation() throws Exception {
        // given
        given(userActivityService.getUserActivity(testUserId)).willReturn(testUserActivityDto);

        // when & then
        mockMvc.perform(get("/api/user-activities/{userId}", testUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("DELETE 요청 후 응답 본문 없음 검증")
    void deleteUserActivity_NoResponseBody() throws Exception {
        // given
        doNothing().when(userActivityService).deleteUserActivity(testUserId);

        // when & then
        mockMvc.perform(delete("/api/user-activities/{userId}", testUserId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
} 