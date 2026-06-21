package com.binitech.shortener.domain.exception;

public class InvalidUrlException extends RuntimeException {

  public InvalidUrlException(String message) {
    super(message);
  }
}
