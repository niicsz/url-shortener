package com.binitech.shortener.adapters.outbound.shortcode;

import com.binitech.shortener.application.ports.outbound.ShortCodeEncoderPort;
import org.hashids.Hashids;
import org.springframework.stereotype.Component;

@Component
public class HashidsShortCodeEncoder implements ShortCodeEncoderPort {

  private final Hashids hashids;

  public HashidsShortCodeEncoder(Hashids hashids) {
    this.hashids = hashids;
  }

  @Override
  public String encode(long id) {
    return hashids.encode(id);
  }
}
