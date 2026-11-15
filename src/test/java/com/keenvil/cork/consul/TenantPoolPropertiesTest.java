package com.keenvil.cork.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

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

  @Test
  void connectionTimeoutKeepsConsulValueByDefault() {
    HikariConfig config = consulValues();
    config.setConnectionTimeout(30_000);

    new TenantPoolProperties().applyTo(config);

    assertEquals(30_000, config.getConnectionTimeout());
  }

  @Test
  void connectionTimeoutOverridesConsulValueWhenSet() {
    HikariConfig config = consulValues();
    config.setConnectionTimeout(1_000);
    TenantPoolProperties pool = new TenantPoolProperties();
    pool.setConnectionTimeout(5_000);

    pool.applyTo(config);

    assertEquals(5_000, config.getConnectionTimeout());
  }

  private static TenantPoolProperties withOverride(String tenant, Integer max, Integer minIdle,
      Long timeout) {
    TenantPoolProperties pool = new TenantPoolProperties();
    pool.setMaximumPoolSize(15);
    pool.setConnectionTimeout(3_000);
    TenantPoolProperties.TenantOverride o = new TenantPoolProperties.TenantOverride();
    o.setMaximumPoolSize(max);
    o.setMinimumIdle(minIdle);
    o.setConnectionTimeout(timeout);
    pool.getTenants().put(tenant, o);
    return pool;
  }

  @Test
  void tenantOverrideWinsOverGlobal() {
    HikariConfig config = consulValues();

    withOverride("cargatest", 25, 2, 5_000L).applyTo(config, "cargatest");

    assertEquals(25, config.getMaximumPoolSize());
    assertEquals(2, config.getMinimumIdle());
    assertEquals(5_000, config.getConnectionTimeout());
  }

  @Test
  void partialOverrideKeepsGlobalForTheRest() {
    HikariConfig config = consulValues();

    withOverride("cargatest", 25, null, null).applyTo(config, "cargatest");

    assertEquals(25, config.getMaximumPoolSize());
    assertEquals(1, config.getMinimumIdle());
    assertEquals(3_000, config.getConnectionTimeout());
  }

  @Test
  void tenantWithoutOverrideUsesGlobal() {
    HikariConfig config = consulValues();

    withOverride("cargatest", 25, null, null).applyTo(config, "elnido");

    assertEquals(15, config.getMaximumPoolSize());
    assertEquals(3_000, config.getConnectionTimeout());
  }

  @Test
  void disabledIgnoresTenantOverride() {
    HikariConfig config = consulValues();
    TenantPoolProperties pool = withOverride("cargatest", 25, null, null);
    pool.setEnabled(false);

    pool.applyTo(config, "cargatest");

    assertEquals(10, config.getMaximumPoolSize());
  }

  @Test
  void emptyPropertyBindsToNullAndKeepsGlobal() {
    // env.template renders an unset Consul key as an empty value: it must not break the binding.
    Map<String, String> props = Map.of(
        "keenvil.multitenancy.pool.maximum-pool-size", "15",
        "keenvil.multitenancy.pool.tenants.cargatest.maximum-pool-size", "",
        "keenvil.multitenancy.pool.tenants.elnido.maximum-pool-size", "20");
    TenantPoolProperties pool = new Binder(new MapConfigurationPropertySource(props))
        .bind("keenvil.multitenancy.pool", TenantPoolProperties.class).get();

    TenantPoolProperties.TenantOverride cargatest = pool.getTenants().get("cargatest");
    assertNull(cargatest == null ? null : cargatest.getMaximumPoolSize());
    HikariConfig config = consulValues();
    pool.applyTo(config, "cargatest");
    assertEquals(15, config.getMaximumPoolSize());
    HikariConfig elnido = consulValues();
    pool.applyTo(elnido, "elnido");
    assertEquals(20, elnido.getMaximumPoolSize());
  }
}
