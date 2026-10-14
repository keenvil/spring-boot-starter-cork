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

  /** Applies these settings over the ones read from Consul. */
  void applyTo(HikariConfig config) {
    if (!enabled) {
      return;
    }
    config.setMaximumPoolSize(maximumPoolSize);
    config.setMinimumIdle(Math.min(minimumIdle, maximumPoolSize));
    config.setIdleTimeout(idleTimeout);
    config.setMaxLifetime(maxLifetime);
    if (connectionTimeout > 0) {
      config.setConnectionTimeout(connectionTimeout);
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
