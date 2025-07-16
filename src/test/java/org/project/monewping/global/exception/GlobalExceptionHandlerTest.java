package org.project.monewping.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.project.monewping.global.dto.ErrorResponse;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * {@link GlobalExceptionHandler}의 예외 처리 로직을 테스트하는 클래스
 * 
 * <p>
 * 전역 예외 처리기의 각 예외 유형별 처리 로직을 단위 테스트로 검증합니다.
 * 예외 처리 메서드들이 올바른 HTTP 상태코드와 에러 메시지를 반환하는지 확인합니다.
 * </p>
 * 
 * <p>
 * 테스트 범위:
 * </p>
 * <ul>
 * <li>Bean Validation 예외 처리</li>
 * <li>이메일 중복 예외 처리</li>
 * <li>일반 예외 처리</li>
 * <li>HTTP 상태코드 검증</li>
 * <li>에러 메시지 검증</li>
 * </ul>
 */
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

  /**
   * Bean Validation 실패 시 예외 처리 로직을 테스트합니다.
   * 
   * <p>
   * MethodArgumentNotValidException 발생 시 400 Bad Request 상태코드와
   * 유효성 검사 실패 메시지가 올바르게 반환되는지 검증합니다.
   * </p>
   */
  @Test
  @DisplayName("유효성 검사 실패 시 BAD_REQUEST를 반환한다")
  void handleValidationExceptions() {
    // given
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("user", "email", "이메일은 필수입니다.");

    given(exception.getBindingResult()).willReturn(bindingResult);
    given(bindingResult.getAllErrors()).willReturn(Collections.singletonList(fieldError));

    // when
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message()).isEqualTo("유효성 검사 실패");
    assertThat(response.getBody().details()).contains("email: 이메일은 필수입니다.");
  }

  /**
   * 이메일 중복 예외 처리 로직을 테스트합니다.
   * 
   * <p>
   * EmailAlreadyExistsException 발생 시 409 Conflict 상태코드와
   * 이메일 중복 메시지가 올바르게 반환되는지 검증합니다.
   * </p>
   */
  @Test
  @DisplayName("이메일 중복 예외 시 CONFLICT를 반환한다")
  void handleEmailAlreadyExistsException() {
    // given
    EmailAlreadyExistsException exception = new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");

    // when
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleEmailAlreadyExistsException(exception);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().message()).isEqualTo("이미 존재하는 이메일입니다.");
  }

  /**
   * 일반 예외 처리 로직을 테스트합니다.
   * 
   * <p>
   * 명시적으로 처리되지 않은 Exception 발생 시 500 Internal Server Error 상태코드와
   * 일반적인 서버 오류 메시지가 올바르게 반환되는지 검증합니다.
   * </p>
   */
  @Test
  @DisplayName("일반 예외 시 INTERNAL_SERVER_ERROR를 반환한다")
  void handleGenericException() {
    // given
    Exception exception = new RuntimeException("서버 오류");

    // when
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().message()).isEqualTo("서버 내부 오류가 발생했습니다.");
    assertThat(response.getBody().details()).isEqualTo("서버 오류");
  }
}