package com.medinote.medinote_back_kc.common.exception;

public class EmailNotVerifiedException extends RuntimeException {
  public EmailNotVerifiedException(String message) {
    super(message);
  }
}
