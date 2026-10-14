package com.keenvil.cork.multitenancy;

import static org.slf4j.LoggerFactory.getLogger;

import javax.sql.DataSource;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.springframework.core.io.ResourceLoader;

/**
 * Runs the service changelog on a tenant database the first time its data source is created.
 *
 * <p>Tenant data sources are loaded lazily from Consul by
 * {@link DataSourceBasedCommunityConnectionProvider}, after {@link MultiTenantSpringLiquibase}
 * ran on the tenants known at startup (in practice only the default one), so without this the
 * tenant schemas never receive new changesets.</p>
 *
 * <p>A failure is logged and swallowed: a broken tenant must not break the request that
 * triggered it nor the other tenants. The schema stays as it was, like before this class.</p>
 */
public class TenantLiquibaseMigrator {

  private static final Logger log = getLogger(TenantLiquibaseMigrator.class);

  private final boolean enabled;

  private final String changeLog;

  private final ResourceLoader resourceLoader;

  public TenantLiquibaseMigrator(boolean enabled, String changeLog,
      ResourceLoader resourceLoader) {
    this.enabled = enabled;
    this.changeLog = changeLog;
    this.resourceLoader = resourceLoader;
    log.info("Liquibase on tenant data sources: enabled={}", enabled);
  }

  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Updates the tenant schema.
   *
   * @return {@code true} if Liquibase ran without errors, {@code false} if it is disabled or failed
   */
  public boolean migrate(String tenant, DataSource dataSource) {
    if (!enabled) {
      return false;
    }
    long start = System.currentTimeMillis();
    try {
      newLiquibase(dataSource).afterPropertiesSet();
      log.info("Liquibase ran for tenant [{}] in {} ms", tenant, System.currentTimeMillis() - start);
      return true;
    } catch (Exception e) {
      log.error("Liquibase failed for tenant [{}], schema left as it was: {}", tenant, e.toString());
      return false;
    }
  }

  SpringLiquibase newLiquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setChangeLog(changeLog);
    liquibase.setShouldRun(true);
    liquibase.setResourceLoader(resourceLoader);
    liquibase.setDataSource(dataSource);
    return liquibase;
  }
}
