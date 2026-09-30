package com.keenvil.cork.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariConfig;

class TenantPoolPropertiesTest {

  private static HikariConfig consulValues() {
    HikariConfig config = new HikariConfig();
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(10);
    return config;
  }

  @Test
  void defaultsWinOverConsulValues() {
    HikariConfig config = consulValues();

    new TenantPoolProperties().applyTo(config);

    assertEquals(5, config.getMaximumPoolSize());
    assertEquals(1, config.getMinimumIdle());
    assertEquals(300_000, config.getIdleTimeout());
    assertEquals(1_800_000, config.getMaxLifetime());
  }

  @Test
  void disabledKeepsConsulValues() {
    HikariConfig config = consulValues();
    TenantPoolProperties pool = new TenantPoolProperties();
    pool.setEnabled(false);

    pool.applyTo(config);

    assertEquals(10, config.getMaximumPoolSize());
    assertEquals(10, config.getMinimumIdle());
  }

  @Test
  void minimumIdleNeverAboveMaximum() {
    HikariConfig config = consulValues();
    TenantPoolProperties pool = new TenantPoolProperties();
    pool.setMaximumPoolSize(3);
    pool.setMinimumIdle(8);

    pool.applyTo(config);

    assertEquals(3, config.getMaximumPoolSize());
    assertEquals(3, config.getMinimumIdle());
  }
}
