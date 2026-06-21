package com.binitech.shortener.adapters.outbound.id;

import com.binitech.shortener.application.ports.outbound.IdGeneratorPort;
import com.binitech.shortener.config.ShortenerProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisIdGeneratorAdapter implements IdGeneratorPort {

  private final StringRedisTemplate redisTemplate;
  private final String counterKey;
  private final long initialId;

  public RedisIdGeneratorAdapter(
      StringRedisTemplate redisTemplate, ShortenerProperties properties) {
    this.redisTemplate = redisTemplate;
    this.counterKey = properties.getCounterKey();
    this.initialId = properties.getInitialId();
  }

  @Override
  public long nextId() {
    redisTemplate.opsForValue().setIfAbsent(counterKey, Long.toString(initialId));
    Long id = redisTemplate.opsForValue().increment(counterKey);
    if (id == null) {
      throw new IllegalStateException("Não foi possível gerar o próximo id no Redis.");
    }
    return id;
  }
}
