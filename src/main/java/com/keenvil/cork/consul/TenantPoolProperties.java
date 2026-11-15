package com.keenvil.cork.consul;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.zaxxer.hikari.HikariConfig;

/**
 * Hikari settings for the per-tenant pools created from Consul.
 *
 * <p>Every tenant gets its own pool in every replica, so the MySQL connections of a service are
 * {@code replicas x tenants x pool size}. The Consul tenant entries carry the old values (about
 * 10 connections held idle per tenant), so these values win over the Consul ones while
 * {@code enabled} is true. {@code keenvil.multitenancy.pool.enabled=false} goes back to the
 * Consul values.</p>
 */
@ConfigurationProperties(prefix = "keenvil.multitenancy.pool")
public class TenantPoolProperties {

  private boolean enabled = true;

  private int maximumPoolSize = 5;

  private int minimumIdle = 1;

  /** Milliseconds an idle connection above {@code minimumIdle} is kept. */
  private long idleTimeout = 300_000;

  /** Milliseconds before a connection is retired; below MySQL's wait_timeout. */
  private long maxLifetime = 1_800_000;

  /**
   * Milliseconds a request waits for a connection of the tenant pool. {@code 0} keeps the value of
   * the tenant entry in Consul, which is not uniform (1000 / 5000 / 30000 ms in prod): with 1 s a
   * burst against a pool that is still opening connections fails with "Connection is not
   * available" (guard-api, 08/10/2026).
   */
  private long connectionTimeout = 0;

  /**
   * Tenants whose pool is created (and its {@code minimumIdle} connections opened) right after
   * startup instead of on the first request. Empty by default: pools stay lazy.
   */
  private java.util.List<String> warmUpTenants = new java.util.ArrayList<>();

  /**
   * Per-tenant overrides ({@code keenvil.multitenancy.pool.tenants.<tenant>.*}) applied over the
   * global values. A null field keeps the global value, so an empty property (a Consul key that
   * is not set) changes nothing. Lets a single tenant get a bigger pool without multiplying the
   * connections of every tenant (load tests on {@code cargatest}, 15/11/2026).
   */
  private java.util.Map<String, TenantOverride> tenants = new java.util.HashMap<>();

  /** Applies these settings over the ones read from Consul. */
  void applyTo(HikariConfig config) {
    applyTo(config, null);
  }

  /** Applies these settings, and the override of {@code tenantId} if any, over the Consul ones. */
  void applyTo(HikariConfig config, String tenantId) {
    if (!enabled) {
      return;
    }
    TenantOverride o = tenantId == null ? null : tenants.get(tenantId);
    int max = o != null && o.getMaximumPoolSize() != null ? o.getMaximumPoolSize() : maximumPoolSize;
    int minIdle = o != null && o.getMinimumIdle() != null ? o.getMinimumIdle() : minimumIdle;
    long timeout = o != null && o.getConnectionTimeout() != null
        ? o.getConnectionTimeout() : connectionTimeout;
    config.setMaximumPoolSize(max);
    config.setMinimumIdle(Math.min(minIdle, max));
    config.setIdleTimeout(idleTimeout);
    config.setMaxLifetime(maxLifetime);
    if (timeout > 0) {
      config.setConnectionTimeout(timeout);
    }
  }

  public java.util.Map<String, TenantOverride> getTenants() {
    return tenants;
  }

  public void setTenants(java.util.Map<String, TenantOverride> tenants) {
    this.tenants = tenants == null ? new java.util.HashMap<>() : tenants;
  }

  /** Pool values for one tenant; null keeps the global value. */
  public static class TenantOverride {

    private Integer maximumPoolSize;

    private Integer minimumIdle;

    private Long connectionTimeout;

    public Integer getMaximumPoolSize() {
      return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
      this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getMinimumIdle() {
      return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
      this.minimumIdle = minimumIdle;
    }

    public Long getConnectionTimeout() {
      return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
      this.connectionTimeout = connectionTimeout;
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  public void setMaximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
  }

  public int getMinimumIdle() {
    return minimumIdle;
  }

  public void setMinimumIdle(int minimumIdle) {
    this.minimumIdle = minimumIdle;
  }

  public long getIdleTimeout() {
    return idleTimeout;
  }

  public void setIdleTimeout(long idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public long getMaxLifetime() {
    return maxLifetime;
  }

  public void setMaxLifetime(long maxLifetime) {
    this.maxLifetime = maxLifetime;
  }

  public long getConnectionTimeout() {
    return connectionTimeout;
  }

  public void setConnectionTimeout(long connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public java.util.List<String> getWarmUpTenants() {
    return warmUpTenants;
  }

  public void setWarmUpTenants(java.util.List<String> warmUpTenants) {
    this.warmUpTenants = warmUpTenants == null ? new java.util.ArrayList<>() : warmUpTenants;
  }
}
