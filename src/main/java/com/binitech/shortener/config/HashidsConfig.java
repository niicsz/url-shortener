package com.binitech.shortener.config;

import org.hashids.Hashids;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashidsConfig {

  @Bean
  public Hashids hashids(ShortenerProperties properties) {
    return new Hashids(properties.getSalt(), properties.getMinLength(), properties.getAlphabet());
  }
}
