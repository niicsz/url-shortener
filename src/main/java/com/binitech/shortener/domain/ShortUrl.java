package com.binitech.shortener.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class ShortUrl implements Serializable {

  private static final long serialVersionUID = 1L;

  private String shortCode;
  private String longUrl;
  private LocalDateTime createdAt;

  public ShortUrl() {}

  public ShortUrl(String shortCode, String longUrl, LocalDateTime createdAt) {
    this.shortCode = shortCode;
    this.longUrl = longUrl;
    this.createdAt = createdAt;
  }

  public String getShortCode() {
    return shortCode;
  }

  public void setShortCode(String shortCode) {
    this.shortCode = shortCode;
  }

  public String getLongUrl() {
    return longUrl;
  }

  public void setLongUrl(String longUrl) {
    this.longUrl = longUrl;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ShortUrl other = (ShortUrl) o;
    return Objects.equals(shortCode, other.shortCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shortCode);
  }

  @Override
  public String toString() {
    return "ShortUrl{shortCode='"
        + shortCode
        + "', longUrl='"
        + longUrl
        + "', createdAt="
        + createdAt
        + '}';
  }
}
