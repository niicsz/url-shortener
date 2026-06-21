package com.binitech.shortener.application;

import java.time.LocalDateTime;

public record ShortenResult(
    String shortCode, String shortUrl, String longUrl, LocalDateTime createdAt) {}
