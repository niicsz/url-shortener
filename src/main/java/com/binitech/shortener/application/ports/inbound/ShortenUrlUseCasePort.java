package com.binitech.shortener.application.ports.inbound;

import com.binitech.shortener.application.ShortenResult;
import com.binitech.shortener.domain.UrlStats;

public interface ShortenUrlUseCasePort {

  ShortenResult shorten(String longUrl);

  String resolve(String shortCode);

  UrlStats getStats(String shortCode);
}
