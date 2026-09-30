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

  /** Applies these settings over the ones read from Consul. */
  void applyTo(HikariConfig config) {
    if (!enabled) {
      return;
    }
    config.setMaximumPoolSize(maximumPoolSize);
    config.setMinimumIdle(Math.min(minimumIdle, maximumPoolSize));
    config.setIdleTimeout(idleTimeout);
    config.setMaxLifetime(maxLifetime);
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
}
