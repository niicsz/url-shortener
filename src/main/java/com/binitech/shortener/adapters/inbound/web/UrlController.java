package com.binitech.shortener.adapters.inbound.web;

import com.binitech.shortener.adapters.inbound.web.generated.api.UrlsApi;
import com.binitech.shortener.adapters.inbound.web.generated.model.ShortenUrlRequest;
import com.binitech.shortener.adapters.inbound.web.generated.model.ShortenUrlResponse;
import com.binitech.shortener.adapters.inbound.web.generated.model.UrlStatsDTO;
import com.binitech.shortener.adapters.inbound.web.mapper.WebMapper;
import com.binitech.shortener.application.ShortenResult;
import com.binitech.shortener.application.ports.inbound.ShortenUrlUseCasePort;
import com.binitech.shortener.domain.UrlStats;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlController implements UrlsApi {

  private final ShortenUrlUseCasePort shortenUrlUseCase;
  private final WebMapper webMapper;

  public UrlController(ShortenUrlUseCasePort shortenUrlUseCase, WebMapper webMapper) {
    this.shortenUrlUseCase = shortenUrlUseCase;
    this.webMapper = webMapper;
  }

  @Override
  public ResponseEntity<ShortenUrlResponse> shortenUrl(ShortenUrlRequest shortenUrlRequest) {
    ShortenResult result = shortenUrlUseCase.shorten(shortenUrlRequest.getUrl());
    return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(result));
  }

  @Override
  public ResponseEntity<UrlStatsDTO> getUrlStats(String shortCode) {
    UrlStats stats = shortenUrlUseCase.getStats(shortCode);
    return ResponseEntity.ok(webMapper.toDto(stats));
  }
}
