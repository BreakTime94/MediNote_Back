package com.medinote.medinote_back_kc.common.exception;

public class DuplicateNicknameException extends RuntimeException {
  public DuplicateNicknameException(String message) {
    super(message);
  }
}
