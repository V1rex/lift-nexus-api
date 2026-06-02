package com.v1rex.liftnexus.config;

import org.testcontainers.containers.PostgreSQLContainer;

public final class SharedPostgresContainer {

  private static final PostgreSQLContainer<?> INSTANCE;

  static {
    INSTANCE =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("warehouse_testdb")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withReuse(true);

    INSTANCE.start();
    Runtime.getRuntime().addShutdownHook(new Thread(INSTANCE::stop));
  }

  public static PostgreSQLContainer<?> getInstance() {
    return INSTANCE;
  }
}
