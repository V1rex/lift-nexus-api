package com.v1rex.liftnexus.config;

import org.testcontainers.containers.PostgreSQLContainer;

public final class SharedPostgresContainer {

  private static final boolean IS_CI = "true".equalsIgnoreCase(System.getenv("CI"));

  private static final PostgreSQLContainer<?> INSTANCE;

  static {
    PostgreSQLContainer<?> container =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("warehouse_testdb")
            .withUsername("test_user")
            .withPassword("test_pass");

    if (!IS_CI) {
      container.withReuse(true);
    }

    INSTANCE = container;
    INSTANCE.start();
  }

  public static PostgreSQLContainer<?> getInstance() {
    return INSTANCE;
  }
}
