package com.binitech.shortener.adapters.inbound.web.mapper;

import com.binitech.shortener.adapters.inbound.web.generated.model.ShortenUrlResponse;
import com.binitech.shortener.adapters.inbound.web.generated.model.UrlStatsDTO;
import com.binitech.shortener.application.ShortenResult;
import com.binitech.shortener.domain.UrlStats;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WebMapper {

  ShortenUrlResponse toResponse(ShortenResult result);

  UrlStatsDTO toDto(UrlStats stats);

  default OffsetDateTime map(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }
}
