package com.binitech.shortener.config;

import com.datastax.oss.driver.api.core.CqlSession;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CassandraSchemaInitializer {

  private static final Logger log = LoggerFactory.getLogger(CassandraSchemaInitializer.class);

  private final CqlSession session;
  private final ShortenerProperties properties;

  public CassandraSchemaInitializer(CqlSession session, ShortenerProperties properties) {
    this.session = session;
    this.properties = properties;
  }

  @PostConstruct
  public void initialize() {
    String keyspace = properties.getKeyspace();
    session.execute(
        "CREATE KEYSPACE IF NOT EXISTS "
            + keyspace
            + " WITH REPLICATION = {'class':'SimpleStrategy','replication_factor':"
            + properties.getReplicationFactor()
            + "}");
    session.execute(
        "CREATE TABLE IF NOT EXISTS "
            + keyspace
            + ".urls (short_code text PRIMARY KEY, long_url text, created_at timestamp)");
    session.execute(
        "CREATE TABLE IF NOT EXISTS "
            + keyspace
            + ".url_clicks (short_code text PRIMARY KEY, clicks counter)");
    log.info("Schema do Cassandra garantido no keyspace '{}'", keyspace);
  }
}
