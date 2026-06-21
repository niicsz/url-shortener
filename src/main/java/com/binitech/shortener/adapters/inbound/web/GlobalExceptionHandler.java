package com.binitech.shortener.adapters.inbound.web;

import com.binitech.shortener.adapters.inbound.web.generated.model.ErrorDTO;
import com.binitech.shortener.domain.exception.BusinessException;
import com.binitech.shortener.domain.exception.InvalidUrlException;
import com.binitech.shortener.domain.exception.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(" "));
    if (message.isBlank()) {
      message = "Dados inválidos. Verifique os campos e tente novamente.";
    }
    log.warn("Erro de validação: {}", message);
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
  }

  @ExceptionHandler(InvalidUrlException.class)
  public ResponseEntity<ErrorDTO> handleInvalidUrl(InvalidUrlException ex) {
    log.warn("URL inválida: {}", ex.getMessage());
    return build(HttpStatus.BAD_REQUEST, "INVALID_URL", ex.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorDTO> handleNotFound(ResourceNotFoundException ex) {
    log.warn("Recurso não encontrado: {}", ex.getMessage());
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorDTO> handleBusiness(BusinessException ex) {
    log.warn("Erro de negócio: {}", ex.getMessage());
    return build(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDTO> handleGeneric(Exception ex) {
    log.error("Erro interno inesperado: {}", ex.getMessage(), ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Erro interno do servidor. Tente novamente mais tarde.");
  }

  private ResponseEntity<ErrorDTO> build(HttpStatus status, String code, String message) {
    ErrorDTO error = new ErrorDTO();
    error.setCode(code);
    error.setMessage(message);
    error.setTimestamp(OffsetDateTime.now());
    return ResponseEntity.status(status).body(error);
  }
}
