package com.binitech.shortener.application.ports.outbound;

public interface ClickAnalyticsPort {

  void incrementClicks(String shortCode);

  long countClicks(String shortCode);
}
