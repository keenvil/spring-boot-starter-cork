package com.keenvil.cork.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import com.keenvil.cork.consul.ConsulService;
import com.keenvil.cork.consul.TenantPoolProperties;

class DataSourceBasedCommunityConnectionProviderTest {

  @Test
  void createsTenantDataSourcesWithThePoolProperties() {
    DataSource tenant = mock(DataSource.class);
    TenantPoolProperties pool = new TenantPoolProperties();
    ConsulService consul = mock(ConsulService.class);
    when(consul.getDatasource("sanfrancisco", pool)).thenReturn(tenant);
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default",
            Map.of("default", mock(DataSource.class)));
    provider.setConsulService(consul);
    provider.setTenantPoolProperties(pool);

    assertSame(tenant, provider.selectDataSource("sanfrancisco"));
  }

  @Test
  void warmUpOpensEachTenantPoolAndSkipsFailures() throws Exception {
    DataSource ok = mock(DataSource.class);
    java.sql.Connection connection = mock(java.sql.Connection.class);
    when(ok.getConnection()).thenReturn(connection);
    DataSource broken = mock(DataSource.class);
    when(broken.getConnection()).thenThrow(new java.sql.SQLException("timeout"));
    TenantPoolProperties pool = new TenantPoolProperties();
    ConsulService consul = mock(ConsulService.class);
    when(consul.getDatasource("sanfrancisco", pool)).thenReturn(ok);
    when(consul.getDatasource("remeros", pool)).thenReturn(broken);
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default",
            Map.of("default", mock(DataSource.class)));
    provider.setConsulService(consul);
    provider.setTenantPoolProperties(pool);

    int warmed = provider.warmUp(java.util.List.of("sanfrancisco", " ", "remeros"));

    assertEquals(1, warmed);
    verify(connection).close();
    assertSame(ok, provider.selectDataSource("sanfrancisco"));
  }

  /** Trazas JDBC por barrio (07/11/2026): Hibernate recibe el pool envuelto; Liquibase y el default, el crudo. */
  @Test
  void decoratorWrapsTheDataSourceHandedToHibernateOncePerTenant() {
    DataSource raw = mock(DataSource.class);
    DataSource defaultRaw = mock(DataSource.class);
    DataSource wrapped = mock(DataSource.class);
    DataSource defaultWrapped = mock(DataSource.class);
    TenantPoolProperties pool = new TenantPoolProperties();
    ConsulService consul = mock(ConsulService.class);
    when(consul.getDatasource("sanfrancisco", pool)).thenReturn(raw);
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default", Map.of("default", defaultRaw));
    provider.setConsulService(consul);
    provider.setTenantPoolProperties(pool);
    java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
    provider.setDataSourceDecorator((tenant, ds) -> {
      calls.incrementAndGet();
      return ds == raw ? wrapped : defaultWrapped;
    });

    assertSame(wrapped, provider.selectDataSource("sanfrancisco"));
    assertSame(wrapped, provider.selectDataSource("sanfrancisco"));
    assertSame(defaultWrapped, provider.selectAnyDataSource());
    assertSame(defaultRaw, provider.getDefaultDataSource());
    assertEquals(2, calls.get());
  }

  @Test
  void withoutDecoratorOrWhenItReturnsNullTheRawDataSourceIsUsed() {
    DataSource defaultRaw = mock(DataSource.class);
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default", Map.of("default", defaultRaw));

    assertSame(defaultRaw, provider.selectAnyDataSource());
    provider.setDataSourceDecorator((tenant, ds) -> null);
    assertSame(defaultRaw, provider.selectAnyDataSource());
    provider.setDataSourceDecorator(null);
    assertSame(defaultRaw, provider.selectAnyDataSource());
  }
}
