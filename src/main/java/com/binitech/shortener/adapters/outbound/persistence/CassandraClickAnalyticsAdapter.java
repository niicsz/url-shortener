package com.binitech.shortener.adapters.outbound.persistence;

import com.binitech.shortener.application.ports.outbound.ClickAnalyticsPort;
import com.binitech.shortener.config.ShortenerProperties;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
public class CassandraClickAnalyticsAdapter implements ClickAnalyticsPort {

  private static final Logger log = LoggerFactory.getLogger(CassandraClickAnalyticsAdapter.class);

  private final CqlSession session;
  private final String clicksTable;

  public CassandraClickAnalyticsAdapter(CqlSession session, ShortenerProperties properties) {
    this.session = session;
    this.clicksTable = properties.getKeyspace() + ".url_clicks";
  }

  @Override
  @Async
  public void incrementClicks(String shortCode) {
    try {
      session.execute(
          SimpleStatement.newInstance(
              "UPDATE " + clicksTable + " SET clicks = clicks + 1 WHERE short_code = ?",
              shortCode));
    } catch (RuntimeException e) {
      log.warn("Falha ao registrar clique para shortCode={}: {}", shortCode, e.getMessage());
    }
  }

  @Override
  public long countClicks(String shortCode) {
    Row row =
        session
            .execute(
                SimpleStatement.newInstance(
                    "SELECT clicks FROM " + clicksTable + " WHERE short_code = ?", shortCode))
            .one();
    return row == null ? 0L : row.getLong("clicks");
  }
}
