package com.keenvil.cork.multitenancy;

import static org.junit.jupiter.api.Assertions.assertSame;
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
}
