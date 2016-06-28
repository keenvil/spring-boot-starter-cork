package io.mycommunity.commons.multitenant;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/** Datasource provider for multiple tenants/communities.
 * 
 * <p>Provides a specific datasource for the community selected for this
 * request.</p>
 */
@Component
public class DataSourceBasedCommunityConnectionProvider
    extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

  private static Logger log =
      getLogger(DataSourceBasedCommunityConnectionProvider.class);

  private static final long serialVersionUID = 1L;

  private String defaultTenant;

  private Map<String, DataSource> dataSourceMapping;

  public DataSourceBasedCommunityConnectionProvider() { }

  public DataSourceBasedCommunityConnectionProvider(String aDefaultTenant,
      Map<String, DataSource> aDataSourceMapping) {
    defaultTenant = aDefaultTenant;
    dataSourceMapping = aDataSourceMapping;
  }
  
  @Override
  protected DataSource selectAnyDataSource() {
    return dataSourceMapping.get(defaultTenant);
  }

  @Override
  protected DataSource selectDataSource(String tenantIdentifier) {
    log.debug(tenantIdentifier);
    return dataSourceMapping.get(tenantIdentifier);
  }

  public DataSource getDefaultDataSource() {
    return dataSourceMapping.get(defaultTenant);
  }
}
