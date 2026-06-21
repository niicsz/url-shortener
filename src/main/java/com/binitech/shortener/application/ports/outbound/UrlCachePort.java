package com.binitech.shortener.application.ports.outbound;

import java.util.Optional;

public interface UrlCachePort {

  Optional<String> get(String shortCode);

  void put(String shortCode, String longUrl);
}
