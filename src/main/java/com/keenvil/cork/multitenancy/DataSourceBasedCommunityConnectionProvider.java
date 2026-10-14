package com.keenvil.cork.multitenancy;

import static org.slf4j.LoggerFactory.getLogger;


import com.keenvil.cork.consul.ConsulService;
import com.keenvil.cork.consul.TenantPoolProperties;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

/**
 * Data Source provider for multiple tenants/communities.
 * <p>
 * <p>Provides a specific data source for the community selected for this
 * request.</p>
 */
@Component
public class DataSourceBasedCommunityConnectionProvider
    extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<String> {

  private static Logger log =
      getLogger(DataSourceBasedCommunityConnectionProvider.class);

  private static final long serialVersionUID = 1L;

  private String defaultTenant;

  private Map<String, DataSource> dataSourceMapping;

  @Autowired
  private ConsulService consulService;

  @Autowired(required = false)
  private TenantLiquibaseMigrator tenantLiquibaseMigrator;

  @Autowired(required = false)
  private TenantPoolProperties tenantPoolProperties;

  public DataSourceBasedCommunityConnectionProvider(
      String theDefaultTenant, Map<String, DataSource> theDataSourceMapping) {
    defaultTenant = theDefaultTenant;
    // Concurrent: tenants are added lazily from request threads.
    dataSourceMapping = new ConcurrentHashMap<>(theDataSourceMapping);
  }

  @Override
  protected DataSource selectAnyDataSource() {
    return dataSourceMapping.get(defaultTenant);
  }

  @Override
  protected DataSource selectDataSource(String tenantIdentifier) {
    if (log.isDebugEnabled()) {
      log.debug("Selecting data source for tenant {}.", tenantIdentifier);
    }

    // computeIfAbsent: the data source is created (and its schema updated) once per tenant,
    // even with concurrent first requests.
    return dataSourceMapping.computeIfAbsent(tenantIdentifier, this::newTenantDataSource);
  }

  private DataSource newTenantDataSource(String tenantIdentifier) {
    DataSource dataSource = consulService.getDatasource(tenantIdentifier, tenantPoolProperties);
    if (tenantLiquibaseMigrator != null) {
      tenantLiquibaseMigrator.migrate(tenantIdentifier, dataSource);
    }
    return dataSource;
  }

  /**
   * Creates the pool of each tenant and borrows one connection from it, so the first requests
   * after a restart do not pay for opening the pool. A tenant that fails is logged and skipped:
   * it will be created lazily on its first request, as before.
   *
   * @param tenants the tenants to warm up.
   * @return how many tenants were warmed up.
   */
  public int warmUp(java.util.Collection<String> tenants) {
    int ok = 0;
    for (String tenant : tenants) {
      String id = tenant == null ? "" : tenant.trim();
      if (id.isEmpty()) {
        continue;
      }
      long start = System.nanoTime();
      try (java.sql.Connection ignored = selectDataSource(id).getConnection()) {
        ok++;
        log.info("Tenant pool {} warmed up in {} ms", id, (System.nanoTime() - start) / 1_000_000);
      } catch (Exception e) {
        log.warn("Tenant pool {} could not be warmed up ({}); it will be created on its first request",
            id, e.getMessage());
      }
    }
    return ok;
  }

  void setConsulService(ConsulService consulService) {
    this.consulService = consulService;
  }

  void setTenantPoolProperties(TenantPoolProperties tenantPoolProperties) {
    this.tenantPoolProperties = tenantPoolProperties;
  }

  void setTenantLiquibaseMigrator(TenantLiquibaseMigrator tenantLiquibaseMigrator) {
    this.tenantLiquibaseMigrator = tenantLiquibaseMigrator;
  }

  public DataSource getDefaultDataSource() {
    return dataSourceMapping.get(defaultTenant);
  }
}
