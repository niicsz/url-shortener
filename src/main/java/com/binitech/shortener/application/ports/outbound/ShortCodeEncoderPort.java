package com.binitech.shortener.application.ports.outbound;

public interface ShortCodeEncoderPort {

  String encode(long id);
}
