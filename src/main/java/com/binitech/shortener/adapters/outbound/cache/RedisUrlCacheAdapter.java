package com.binitech.shortener.adapters.outbound.cache;

import com.binitech.shortener.application.ports.outbound.UrlCachePort;
import com.binitech.shortener.config.ShortenerProperties;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUrlCacheAdapter implements UrlCachePort {

  private static final String KEY_PREFIX = "url:code:";

  private final StringRedisTemplate redisTemplate;
  private final Duration cacheTtl;

  public RedisUrlCacheAdapter(StringRedisTemplate redisTemplate, ShortenerProperties properties) {
    this.redisTemplate = redisTemplate;
    this.cacheTtl = properties.getCacheTtl();
  }

  @Override
  public Optional<String> get(String shortCode) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + shortCode));
  }

  @Override
  public void put(String shortCode, String longUrl) {
    redisTemplate.opsForValue().set(KEY_PREFIX + shortCode, longUrl, cacheTtl);
  }
}
