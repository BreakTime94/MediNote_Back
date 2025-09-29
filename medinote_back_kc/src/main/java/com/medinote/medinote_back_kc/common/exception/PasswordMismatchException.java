package com.medinote.medinote_back_kc.common.exception;

public class PasswordMismatchException extends RuntimeException {
  public PasswordMismatchException(String message) {
    super(message);
  }
}
