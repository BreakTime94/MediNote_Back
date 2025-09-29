package com.medinote.medinote_back_kc.common;

import com.medinote.medinote_back_kc.common.exception.DuplicateEmailException;
import com.medinote.medinote_back_kc.common.exception.DuplicateNicknameException;
import com.medinote.medinote_back_kc.common.exception.NotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
  // 1. 이메일 중복
  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<?> handleDuplicateEmail(DuplicateEmailException ex) {
    log.error("이메일이 중복됩니다! {} {}", ex, ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "status", "ERROR",
            "code", "DUPLICATE_EMAIL",
            "message", ex.getMessage()
    ));
  }

  // 2. 닉네임 중복
  @ExceptionHandler(DuplicateNicknameException.class)
  public ResponseEntity<?> handleDuplicateNickname(DuplicateNicknameException ex) {
    log.error("닉네임이 중복됩니다! {} {}", ex, ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "status", "ERROR",
            "code", "DUPLICATE_NICKNAME",
            "message", ex.getMessage()
    ));
  }

  // 3. DTO 유효성 검증 실패 (@Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
    List<String> errorMessages = ex.getBindingResult()
            .getAllErrors()
            .stream()
            .map(ObjectError::getDefaultMessage)
            .toList();

    log.error("서버 내부 오류 발생 {} {}", ex, errorMessages.toString());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "status", "ERROR",
            "code", "VALIDATION_FAILED",
            "messages", "회원가입 양식이 올바르지 않습니다."
    ));
  }
  //4. NotFoundException? 얘는 무슨용도일까?
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<?> handleNotFound(NotFoundException ex) {
    log.error("NotFoundException {} {}", ex, ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "status", "ERROR",
            "code", "NOT_FOUND",
            "messages", "사용자를 찾을 수 없습니다."
    ));
  }

  //JWTToken 관련해서도 처리해야함.

  // 그 외 모든 에러
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneralException(Exception ex) {
    log.error("서버 내부 오류 발생 {} {}", ex, ex.getMessage());
    // 외부로 응답할 때는 이 body에 담긴 값으로 확인한다.
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "status", "ERROR",
            "code", "INTERNAL_ERROR",
            "message", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    ));
  }
}
