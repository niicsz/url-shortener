package com.binitech.shortener.config;

import com.binitech.shortener.application.ports.inbound.ShortenUrlUseCasePort;
import com.binitech.shortener.application.ports.outbound.ClickAnalyticsPort;
import com.binitech.shortener.application.ports.outbound.IdGeneratorPort;
import com.binitech.shortener.application.ports.outbound.ShortCodeEncoderPort;
import com.binitech.shortener.application.ports.outbound.UrlCachePort;
import com.binitech.shortener.application.ports.outbound.UrlRepositoryPort;
import com.binitech.shortener.application.usecases.ShortenUrlUseCaseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  private static final Logger log = LoggerFactory.getLogger(BeanConfig.class);

  @Bean
  public ShortenUrlUseCasePort shortenUrlUseCasePort(
      UrlRepositoryPort urlRepositoryPort,
      IdGeneratorPort idGeneratorPort,
      ShortCodeEncoderPort shortCodeEncoderPort,
      UrlCachePort urlCachePort,
      ClickAnalyticsPort clickAnalyticsPort,
      ShortenerProperties properties) {
    log.info("Configurando ShortenUrlUseCasePort com baseUrl={}", properties.getBaseUrl());
    return new ShortenUrlUseCaseImpl(
        urlRepositoryPort,
        idGeneratorPort,
        shortCodeEncoderPort,
        urlCachePort,
        clickAnalyticsPort,
        properties.getBaseUrl());
  }
}
