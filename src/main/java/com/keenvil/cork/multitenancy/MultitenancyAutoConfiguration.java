package com.keenvil.cork.multitenancy;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/** Multi Tenancy Auto Configuration.
 *
 * <p>Multi Tenancy in Keenvil context refers to Communities, where each
 * Community is represented  by a Tenant. Community and Tenant will be use
 * indistinctly.</p>
 * 
 * <p>Auto configuration is fired only if {@link MultitenancyCondition}
 * is met.</p>
 * 
 * <p>Multi Tenancy configuration comprises
 * <ul>
 * <li>DataSourceBasedCommunityConnectionProvider with a DefaultDataSource,</li>
 * which is basically a Dictionary to map a Community to its proper Data Source.
 * <li>LocalContainerEntityManagerFactoryBean, a JPA 
 * {@link EntityManagerFactory} to manage Community domain objects.</li>
 * <li>and a CurrentTenantIdentifierResolver, to identify current request
 * Community.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableConfigurationProperties({
      MultitenancyConfigurationProperties.class,
      JpaProperties.class})
@Conditional(value = MultitenancyCondition.class)
public class MultitenancyAutoConfiguration {

  @Value("${keenvil-boot-starter.community-resolver:''}")
  private String communityResolver;

  @Autowired
  private MultitenancyConfigurationProperties multitenancyProperties;

  @Autowired
  private MultitenancySpecification multitenancySpecification;

  @Autowired
  private MultiTenantConnectionProvider multiTenantConnectionProvider;
  
  @Autowired
  private CurrentTenantIdentifierResolver currentTenantIdentifierResolver;

  @Value("${application.hibernate.dialect}")
  private String dialect;

  @Value("${application.hibernate.hbm2ddl}")
  private String hbm2ddl;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JpaProperties jpaProperties;

  /**
   * Multi Tenancy connection provider bean.
   * 
   * @return the Tenant connection provider bean.
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
              .url(tc.getUrl())
              .putAll(tc.getDataSourceProperties())
              .build()
            ));
    
    return new DataSourceBasedCommunityConnectionProvider(
        multitenancyProperties.getDefaultTenant().getName(), dataSources);
  }

  @Bean
  @DependsOn("dataSourceBasedCommunityConnectionProvider")
  public DataSource defaultDataSource() {
    return dataSourceBasedMultiTenantConnectionProvider()
        .getDefaultDataSource();
  }

  /**
   * Creates a {@code LocalContainerEntityManagerFactoryBean} to work with
   * Multi Tenancy approach.
   * 
   * @param builder the builder
   * @return new {@code LocalContainerEntityManagerFactoryBean}
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean
      entityManagerFactory(EntityManagerFactoryBuilder builder) {

    Map<String, Object> hibernateProps = new LinkedHashMap<>();
    hibernateProps.putAll(jpaProperties.getHibernateProperties(dataSource));

    hibernateProps.put(MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
    hibernateProps.put(MULTI_TENANT_CONNECTION_PROVIDER,
        multiTenantConnectionProvider);
    hibernateProps.put(MULTI_TENANT_IDENTIFIER_RESOLVER,
        currentTenantIdentifierResolver);
    hibernateProps.put(DIALECT, dialect);
    hibernateProps.put(HBM2DDL_AUTO, hbm2ddl);

    return builder.dataSource(dataSource)
        .packages(multitenancySpecification.getBasePackages())
        .properties(hibernateProps)
        .jta(false)
        .build();
  }

  /**
   * Current Tenant identifier resolver.
   * 
   * @return identifier resolver.
   */
  @Bean
  public CurrentTenantIdentifierResolver currentCommunityIdentifierResolver() {
    return new CurrentTenantResolver();
  }
}
