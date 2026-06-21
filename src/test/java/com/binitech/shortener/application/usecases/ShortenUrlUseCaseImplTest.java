package com.binitech.shortener.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.binitech.shortener.application.ShortenResult;
import com.binitech.shortener.application.ports.outbound.ClickAnalyticsPort;
import com.binitech.shortener.application.ports.outbound.IdGeneratorPort;
import com.binitech.shortener.application.ports.outbound.ShortCodeEncoderPort;
import com.binitech.shortener.application.ports.outbound.UrlCachePort;
import com.binitech.shortener.application.ports.outbound.UrlRepositoryPort;
import com.binitech.shortener.domain.ShortUrl;
import com.binitech.shortener.domain.UrlStats;
import com.binitech.shortener.domain.exception.InvalidUrlException;
import com.binitech.shortener.domain.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShortenUrlUseCaseImplTest {

  @Mock private UrlRepositoryPort urlRepository;
  @Mock private IdGeneratorPort idGenerator;
  @Mock private ShortCodeEncoderPort shortCodeEncoder;
  @Mock private UrlCachePort urlCache;
  @Mock private ClickAnalyticsPort clickAnalytics;

  private ShortenUrlUseCaseImpl useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new ShortenUrlUseCaseImpl(
            urlRepository,
            idGenerator,
            shortCodeEncoder,
            urlCache,
            clickAnalytics,
            "http://localhost:8080/");
  }

  @Test
  void shortenGeneratesIdEncodesPersistsAndCaches() {
    when(idGenerator.nextId()).thenReturn(14_000_001L);
    when(shortCodeEncoder.encode(14_000_001L)).thenReturn("kVj9pQ2");

    ShortenResult result = useCase.shorten("https://example.com/path");

    assertThat(result.shortCode()).isEqualTo("kVj9pQ2");
    assertThat(result.shortUrl()).isEqualTo("http://localhost:8080/kVj9pQ2");
    verify(urlRepository).save(any(ShortUrl.class));
    verify(urlCache).put("kVj9pQ2", "https://example.com/path");
  }

  @Test
  void shortenAddsHttpsWhenSchemeMissing() {
    when(idGenerator.nextId()).thenReturn(1L);
    when(shortCodeEncoder.encode(1L)).thenReturn("abcdefg");

    ShortenResult result = useCase.shorten("example.com");

    assertThat(result.longUrl()).isEqualTo("https://example.com");
  }

  @Test
  void shortenRejectsBlankUrl() {
    assertThatThrownBy(() -> useCase.shorten("   ")).isInstanceOf(InvalidUrlException.class);
  }

  @Test
  void shortenRejectsNonHttpScheme() {
    assertThatThrownBy(() -> useCase.shorten("ftp://example.com"))
        .isInstanceOf(InvalidUrlException.class);
  }

  @Test
  void resolveReturnsCachedValueAndCountsClick() {
    when(urlCache.get("kVj9pQ2")).thenReturn(Optional.of("https://example.com"));

    String longUrl = useCase.resolve("kVj9pQ2");

    assertThat(longUrl).isEqualTo("https://example.com");
    verify(urlRepository, never()).findByShortCode(anyString());
    verify(clickAnalytics).incrementClicks("kVj9pQ2");
  }

  @Test
  void resolveFallsBackToRepositoryOnCacheMiss() {
    when(urlCache.get("kVj9pQ2")).thenReturn(Optional.empty());
    when(urlRepository.findByShortCode("kVj9pQ2"))
        .thenReturn(
            Optional.of(new ShortUrl("kVj9pQ2", "https://example.com", LocalDateTime.now())));

    String longUrl = useCase.resolve("kVj9pQ2");

    assertThat(longUrl).isEqualTo("https://example.com");
    verify(urlCache).put("kVj9pQ2", "https://example.com");
    verify(clickAnalytics).incrementClicks("kVj9pQ2");
  }

  @Test
  void resolveThrowsWhenNotFound() {
    when(urlCache.get("missing")).thenReturn(Optional.empty());
    when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.resolve("missing"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getStatsCombinesUrlAndClickCount() {
    LocalDateTime createdAt = LocalDateTime.now();
    when(urlRepository.findByShortCode("kVj9pQ2"))
        .thenReturn(Optional.of(new ShortUrl("kVj9pQ2", "https://example.com", createdAt)));
    when(clickAnalytics.countClicks("kVj9pQ2")).thenReturn(42L);

    UrlStats stats = useCase.getStats("kVj9pQ2");

    assertThat(stats.getClicks()).isEqualTo(42L);
    assertThat(stats.getLongUrl()).isEqualTo("https://example.com");
    assertThat(stats.getCreatedAt()).isEqualTo(createdAt);
  }
}
