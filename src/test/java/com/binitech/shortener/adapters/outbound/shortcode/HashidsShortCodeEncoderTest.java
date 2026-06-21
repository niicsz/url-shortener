package com.binitech.shortener.adapters.outbound.shortcode;

import static org.assertj.core.api.Assertions.assertThat;

import org.hashids.Hashids;
import org.junit.jupiter.api.Test;

class HashidsShortCodeEncoderTest {

  private static final String ALPHABET =
      "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private final HashidsShortCodeEncoder encoder =
      new HashidsShortCodeEncoder(new Hashids("binitech-url-shortener", 7, ALPHABET));

  @Test
  void encodesToAtLeastMinLength() {
    assertThat(encoder.encode(14_000_001L).length()).isGreaterThanOrEqualTo(7);
  }

  @Test
  void usesOnlyBase62Alphabet() {
    assertThat(encoder.encode(987_654_321L)).matches("[0-9a-zA-Z]+");
  }

  @Test
  void producesDistinctCodesForDistinctIds() {
    assertThat(encoder.encode(1L)).isNotEqualTo(encoder.encode(2L));
  }

  @Test
  void isDeterministic() {
    assertThat(encoder.encode(42L)).isEqualTo(encoder.encode(42L));
  }
}
