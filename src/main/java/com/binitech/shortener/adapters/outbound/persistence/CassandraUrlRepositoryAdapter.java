package com.binitech.shortener.adapters.outbound.persistence;

import com.binitech.shortener.application.ports.outbound.UrlRepositoryPort;
import com.binitech.shortener.config.ShortenerProperties;
import com.binitech.shortener.domain.ShortUrl;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CassandraUrlRepositoryAdapter implements UrlRepositoryPort {

  private final CqlSession session;
  private final String urlsTable;

  public CassandraUrlRepositoryAdapter(CqlSession session, ShortenerProperties properties) {
    this.session = session;
    this.urlsTable = properties.getKeyspace() + ".urls";
  }

  @Override
  public void save(ShortUrl shortUrl) {
    Instant createdAt = shortUrl.getCreatedAt().toInstant(ZoneOffset.UTC);
    session.execute(
        SimpleStatement.newInstance(
            "INSERT INTO " + urlsTable + " (short_code, long_url, created_at) VALUES (?, ?, ?)",
            shortUrl.getShortCode(),
            shortUrl.getLongUrl(),
            createdAt));
  }

  @Override
  public Optional<ShortUrl> findByShortCode(String shortCode) {
    Row row =
        session
            .execute(
                SimpleStatement.newInstance(
                    "SELECT short_code, long_url, created_at FROM "
                        + urlsTable
                        + " WHERE short_code = ?",
                    shortCode))
            .one();
    if (row == null) {
      return Optional.empty();
    }
    Instant createdAt = row.getInstant("created_at");
    LocalDateTime created =
        createdAt == null ? null : LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC);
    return Optional.of(
        new ShortUrl(row.getString("short_code"), row.getString("long_url"), created));
  }
}
