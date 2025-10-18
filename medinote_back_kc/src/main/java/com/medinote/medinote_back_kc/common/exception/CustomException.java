package com.medinote.medinote_back_kc.common.exception;

import com.medinote.medinote_back_kc.common.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
