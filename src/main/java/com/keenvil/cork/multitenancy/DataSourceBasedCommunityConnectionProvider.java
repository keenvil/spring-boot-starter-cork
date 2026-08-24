package com.keenvil.cork.multitenancy;

import static org.slf4j.LoggerFactory.getLogger;


import com.keenvil.cork.consul.ConsulService;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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

  public DataSourceBasedCommunityConnectionProvider(
      String theDefaultTenant, Map<String, DataSource> theDataSourceMapping) {
    defaultTenant = theDefaultTenant;
    dataSourceMapping = theDataSourceMapping;
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

    if (dataSourceMapping.get(tenantIdentifier) == null) {
        dataSourceMapping.put(
            tenantIdentifier, consulService.getDatasource(tenantIdentifier));

    }
    return dataSourceMapping.get(tenantIdentifier);
  }

  public DataSource getDefaultDataSource() {
    return dataSourceMapping.get(defaultTenant);
  }
}
