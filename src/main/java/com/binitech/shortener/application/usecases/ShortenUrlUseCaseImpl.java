package com.binitech.shortener.application.usecases;

import com.binitech.shortener.application.ShortenResult;
import com.binitech.shortener.application.ports.inbound.ShortenUrlUseCasePort;
import com.binitech.shortener.application.ports.outbound.ClickAnalyticsPort;
import com.binitech.shortener.application.ports.outbound.IdGeneratorPort;
import com.binitech.shortener.application.ports.outbound.ShortCodeEncoderPort;
import com.binitech.shortener.application.ports.outbound.UrlCachePort;
import com.binitech.shortener.application.ports.outbound.UrlRepositoryPort;
import com.binitech.shortener.domain.ShortUrl;
import com.binitech.shortener.domain.UrlStats;
import com.binitech.shortener.domain.exception.InvalidUrlException;
import com.binitech.shortener.domain.exception.ResourceNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShortenUrlUseCaseImpl implements ShortenUrlUseCasePort {

  private static final Logger log = LoggerFactory.getLogger(ShortenUrlUseCaseImpl.class);
  private static final int MAX_URL_LENGTH = 2048;

  private final UrlRepositoryPort urlRepository;
  private final IdGeneratorPort idGenerator;
  private final ShortCodeEncoderPort shortCodeEncoder;
  private final UrlCachePort urlCache;
  private final ClickAnalyticsPort clickAnalytics;
  private final String baseUrl;

  public ShortenUrlUseCaseImpl(
      UrlRepositoryPort urlRepository,
      IdGeneratorPort idGenerator,
      ShortCodeEncoderPort shortCodeEncoder,
      UrlCachePort urlCache,
      ClickAnalyticsPort clickAnalytics,
      String baseUrl) {
    this.urlRepository = urlRepository;
    this.idGenerator = idGenerator;
    this.shortCodeEncoder = shortCodeEncoder;
    this.urlCache = urlCache;
    this.clickAnalytics = clickAnalytics;
    this.baseUrl = stripTrailingSlash(baseUrl);
  }

  @Override
  public ShortenResult shorten(String longUrl) {
    String normalizedUrl = normalizeAndValidate(longUrl);

    long id = idGenerator.nextId();
    String shortCode = shortCodeEncoder.encode(id);
    LocalDateTime createdAt = LocalDateTime.now();

    urlRepository.save(new ShortUrl(shortCode, normalizedUrl, createdAt));
    urlCache.put(shortCode, normalizedUrl);

    if (log.isInfoEnabled()) {
      log.info("URL encurtada: id={} shortCode={}", id, shortCode);
    }

    return new ShortenResult(shortCode, baseUrl + "/" + shortCode, normalizedUrl, createdAt);
  }

  @Override
  public String resolve(String shortCode) {
    Optional<String> cached = urlCache.get(shortCode);
    if (cached.isPresent()) {
      if (log.isDebugEnabled()) {
        log.debug("Cache hit para shortCode={}", shortCode);
      }
      clickAnalytics.incrementClicks(shortCode);
      return cached.get();
    }

    ShortUrl shortUrl =
        urlRepository
            .findByShortCode(shortCode)
            .orElseThrow(
                () -> {
                  if (log.isWarnEnabled()) {
                    log.warn("ShortCode não encontrado: {}", shortCode);
                  }
                  return new ResourceNotFoundException("URL", "shortCode", shortCode);
                });

    urlCache.put(shortCode, shortUrl.getLongUrl());
    clickAnalytics.incrementClicks(shortCode);
    if (log.isDebugEnabled()) {
      log.debug("Cache miss resolvido pelo Cassandra para shortCode={}", shortCode);
    }
    return shortUrl.getLongUrl();
  }

  @Override
  public UrlStats getStats(String shortCode) {
    ShortUrl shortUrl =
        urlRepository
            .findByShortCode(shortCode)
            .orElseThrow(() -> new ResourceNotFoundException("URL", "shortCode", shortCode));

    long clicks = clickAnalytics.countClicks(shortCode);
    return new UrlStats(shortCode, shortUrl.getLongUrl(), shortUrl.getCreatedAt(), clicks);
  }

  private String normalizeAndValidate(String rawUrl) {
    if (rawUrl == null || rawUrl.isBlank()) {
      throw new InvalidUrlException("A URL não pode ser vazia.");
    }
    String trimmed = rawUrl.trim();
    if (trimmed.length() > MAX_URL_LENGTH) {
      throw new InvalidUrlException(
          "A URL excede o tamanho máximo de " + MAX_URL_LENGTH + " caracteres.");
    }
    String candidate = hasHttpScheme(trimmed) ? trimmed : "https://" + trimmed;
    URI uri;
    try {
      uri = new URI(candidate);
    } catch (URISyntaxException e) {
      throw new InvalidUrlException("URL inválida.");
    }
    String scheme = uri.getScheme();
    if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
      throw new InvalidUrlException("Apenas URLs http e https são suportadas.");
    }
    if (uri.getHost() == null || uri.getHost().isBlank()) {
      throw new InvalidUrlException("URL inválida: host ausente.");
    }
    return candidate;
  }

  private boolean hasHttpScheme(String url) {
    String lower = url.toLowerCase(Locale.ROOT);
    return lower.startsWith("http://") || lower.startsWith("https://");
  }

  private String stripTrailingSlash(String value) {
    if (value != null && value.endsWith("/")) {
      return value.substring(0, value.length() - 1);
    }
    return value;
  }
}
