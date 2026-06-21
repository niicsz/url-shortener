package com.binitech.shortener.application.ports.outbound;

import com.binitech.shortener.domain.ShortUrl;
import java.util.Optional;

public interface UrlRepositoryPort {

  void save(ShortUrl shortUrl);

  Optional<ShortUrl> findByShortCode(String shortCode);
}
