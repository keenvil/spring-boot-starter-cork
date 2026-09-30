package com.keenvil.cork.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.keenvil.cork.consul.ConsulService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class TenantLiquibaseMigratorTest {

  private static final String CHANGELOG = "classpath:db/changelog/tenant-test-changelog.xml";

  private static DataSource h2(String name) {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
    return ds;
  }

  private static boolean hasTable(DataSource ds, String table) throws SQLException {
    try (Connection c = ds.getConnection();
         ResultSet rs = c.getMetaData().getTables(null, null, table.toUpperCase(), null)) {
      return rs.next();
    }
  }

  @Test
  void runsTheChangelogOnTheTenantDatabase() throws Exception {
    DataSource ds = h2("tenant_a");
    TenantLiquibaseMigrator migrator =
        new TenantLiquibaseMigrator(true, CHANGELOG, new DefaultResourceLoader());

    assertTrue(migrator.migrate("tenant_a", ds));
    assertTrue(hasTable(ds, "tenant_marker"));
    // idempotent: a second run finds nothing pending
    assertTrue(migrator.migrate("tenant_a", ds));
  }

  @Test
  void disabledDoesNothing() throws Exception {
    DataSource ds = h2("tenant_b");
    TenantLiquibaseMigrator migrator =
        new TenantLiquibaseMigrator(false, CHANGELOG, new DefaultResourceLoader());

    assertFalse(migrator.migrate("tenant_b", ds));
    assertFalse(hasTable(ds, "tenant_marker"));
  }

  @Test
  void aBrokenTenantDoesNotThrow() throws Exception {
    DataSource broken = mock(DataSource.class);
    when(broken.getConnection()).thenThrow(new SQLException("Unknown database"));
    TenantLiquibaseMigrator migrator =
        new TenantLiquibaseMigrator(true, CHANGELOG, new DefaultResourceLoader());

    assertFalse(migrator.migrate("broken", broken));
  }

  @Test
  void providerCreatesAndMigratesEachTenantOnceUnderConcurrency() throws Exception {
    ConsulService consul = mock(ConsulService.class);
    DataSource tenantDs = h2("tenant_c");
    when(consul.getDatasource("tenant_c", null)).thenAnswer(inv -> {
      Thread.sleep(50);
      return tenantDs;
    });
    TenantLiquibaseMigrator migrator = mock(TenantLiquibaseMigrator.class);
    Map<String, DataSource> initial = new HashMap<>();
    initial.put("default", h2("default_t"));
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default", initial);
    provider.setConsulService(consul);
    provider.setTenantLiquibaseMigrator(migrator);

    ExecutorService pool = Executors.newFixedThreadPool(8);
    CountDownLatch go = new CountDownLatch(1);
    for (int i = 0; i < 8; i++) {
      pool.submit(() -> {
        go.await();
        assertSame(tenantDs, provider.selectDataSource("tenant_c"));
        return null;
      });
    }
    go.countDown();
    pool.shutdown();
    assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

    verify(consul, times(1)).getDatasource("tenant_c", null);
    verify(migrator, times(1)).migrate(eq("tenant_c"), eq(tenantDs));
    // the default tenant was created at startup: never migrated here
    assertSame(initial.get("default"), provider.selectDataSource("default"));
    verify(migrator, never()).migrate(eq("default"), any());
  }

  @Test
  void providerWithoutMigratorStillWorks() {
    ConsulService consul = mock(ConsulService.class);
    DataSource tenantDs = h2("tenant_d");
    when(consul.getDatasource("tenant_d", null)).thenReturn(tenantDs);
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default", new HashMap<>());
    provider.setConsulService(consul);

    assertSame(tenantDs, provider.selectDataSource("tenant_d"));
    assertEquals(tenantDs, provider.selectDataSource("tenant_d"));
    verify(consul, times(1)).getDatasource("tenant_d", null);
  }
}
