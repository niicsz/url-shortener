package com.binitech.shortener.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class UrlStats implements Serializable {

  private static final long serialVersionUID = 1L;

  private String shortCode;
  private String longUrl;
  private LocalDateTime createdAt;
  private long clicks;

  public UrlStats() {}

  public UrlStats(String shortCode, String longUrl, LocalDateTime createdAt, long clicks) {
    this.shortCode = shortCode;
    this.longUrl = longUrl;
    this.createdAt = createdAt;
    this.clicks = clicks;
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

  public long getClicks() {
    return clicks;
  }

  public void setClicks(long clicks) {
    this.clicks = clicks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UrlStats other = (UrlStats) o;
    return Objects.equals(shortCode, other.shortCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shortCode);
  }

  @Override
  public String toString() {
    return "UrlStats{shortCode='"
        + shortCode
        + "', longUrl='"
        + longUrl
        + "', createdAt="
        + createdAt
        + ", clicks="
        + clicks
        + '}';
  }
}
