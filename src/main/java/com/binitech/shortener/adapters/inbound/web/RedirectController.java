package com.binitech.shortener.adapters.inbound.web;

import com.binitech.shortener.application.ports.inbound.ShortenUrlUseCasePort;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

  private final ShortenUrlUseCasePort shortenUrlUseCase;

  public RedirectController(ShortenUrlUseCasePort shortenUrlUseCase) {
    this.shortenUrlUseCase = shortenUrlUseCase;
  }

  @GetMapping("/{shortCode:[A-Za-z0-9]{7,}}")
  public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
    String longUrl = shortenUrlUseCase.resolve(shortCode);
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();
  }
}
