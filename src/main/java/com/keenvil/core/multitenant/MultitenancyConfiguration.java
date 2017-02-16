package com.keenvil.core.multitenant;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/** Multi tenant/community configuration.
 *
 * <p>Multi tenant/community configuration comprises a Community Resolver
 * and DataSource Provider. The first one identifies which community was
 * selected for this request, the latest provides the data source for that
 * community.</p>
 */
@Configuration
@EnableConfigurationProperties(MultitenancyConfigurationProperties.class)
public class MultitenancyConfiguration {

  @Autowired
  private MultitenancyConfigurationProperties multitenancyProperties;

  /** Multi tenant connection provider bean.
   * @return the tenant connection provider bean.
   */
  @Bean(name = "dataSourceBasedCommunityConnectionProvider")
  public DataSourceBasedCommunityConnectionProvider
      dataSourceBasedMultiTenantConnectionProvider() {
    Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
    
    multitenancyProperties.getTenants()
        .stream()
        .forEach(tc -> dataSources.put(tc.getName(),
          DataSourceBuilder.create()
            .driverClassName(tc.getDriverClassName())
            .username(tc.getUsername())
            .password(tc.getPassword())
            .url(tc.getUrl()).build()));
    
    return new DataSourceBasedCommunityConnectionProvider(
        multitenancyProperties.getDefaultTenant().getName(), dataSources);
  }

  @Bean
  @DependsOn("dataSourceBasedCommunityConnectionProvider")
  public DataSource defaultDataSource() {
    return dataSourceBasedMultiTenantConnectionProvider()
        .getDefaultDataSource();
  }

  @Bean
  public CurrentCommunityIdentifierResolver
      currentCommunityIdentifierResolver() {
    return new CurrentCommunityIdentifierResolver();
  }

  @Bean
  public UrlPathVariableCommunityResolver
    urlPathVariableCommunityResolver() {
    return new UrlPathVariableCommunityResolver();
  }
}
