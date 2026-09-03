package com.keenvil.cork.multitenancy;

import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER;
import static org.hibernate.cfg.AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.*;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
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
// Must run BEFORE HibernateJpaAutoConfiguration: Boot's HibernateJpaConfiguration
// gates its @Bean methods (including EntityManagerFactoryBuilder) behind
// @ConditionalOnSingleCandidate(DataSource.class), evaluated against bean
// DEFINITIONS already registered at that point -- not instances. When apps
// exclude Boot's own DataSourceAutoConfiguration (required to avoid it
// colliding by bean name with defaultDataSource() below), the only DataSource
// candidate is defaultDataSource() itself. If this class were ordered after
// HibernateJpaAutoConfiguration (as it used to be, back when Boot's own
// DataSourceAutoConfiguration -- not yet excluded -- supplied that early
// candidate instead), defaultDataSource()'s bean definition would not exist
// yet, the condition would find zero candidates, HibernateJpaConfiguration
// would silently never register, and every @Bean method needing
// EntityManagerFactoryBuilder would fail at runtime with "No qualifying bean".
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
public class MultitenancyAutoConfiguration {

  private static Logger log = getLogger(MultitenancyAutoConfiguration.class);

  @Value("${keenvil-boot-starter.community-resolver:''}")
  private String communityResolver;

  @Autowired
  private MultitenancyConfigurationProperties multitenancyProperties;

  @Autowired
  private MultitenancySpecification multitenancySpecification;

  @Value("${application.hibernate.dialect}")
  private String dialect;

  @Value("${application.hibernate.hbm2ddl}")
  private String hbm2ddl;

  @Autowired
  private JpaProperties jpaProperties;

  @Value("${spring.liquibase.changeLog}")
  private String liquibaseChangelogUrl;

  /**
   * Multi Tenancy connection provider bean.
   * 
   * @return the Tenant connection provider bean.
   */
  @Bean(name = "dataSourceBasedCommunityConnectionProvider")
  public DataSourceBasedCommunityConnectionProvider
  dataSourceBasedMultiTenantConnectionProvider() {
    Map<String, DataSource> dataSources = new HashMap<>();

    multitenancyProperties.getTenants()
      .forEach(tc -> dataSources.put(tc.getName(),
        DataSourceBuilder.create()
          .driverClassName(tc.getDriverClassName())
          .username(tc.getUsername())
          .password(tc.getPassword())
          .url(tc.getJdbcUrl())
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
   * <p>{@code multiTenantConnectionProvider}, {@code currentTenantIdentifierResolver}
   * and {@code dataSource} are resolved as METHOD PARAMETERS on purpose, not as
   * {@code @Autowired} fields on this class: all three are beans produced by
   * {@code @Bean} methods of this very {@code @Configuration} class
   * ({@code dataSourceBasedMultiTenantConnectionProvider}, {@code currentCommunityIdentifierResolver}
   * and {@code defaultDataSource}). As fields, Spring must fully populate this
   * bean's properties before it can invoke any of its own {@code @Bean} methods on
   * itself -- but resolving those very fields requires calling those very
   * methods, so the container detects an unresolvable circular reference
   * (only surfaced starting with Boot 3, which no longer tolerates it by
   * default) and startup fails. Method-parameter injection for {@code @Bean}
   * factory methods is resolved lazily, when the method itself runs, which
   * sidesteps the ordering trap entirely -- the standard Spring fix for this
   * exact self-referencing-configuration pattern, no {@code allow-circular-references}
   * escape hatch needed.
   *
   * @param builder the builder
   * @param multiTenantConnectionProvider the tenant connection provider
   * @param currentTenantIdentifierResolver the tenant identifier resolver
   * @param dataSource the default data source
   * @return new {@code LocalContainerEntityManagerFactoryBean}
   */
  @Bean
  public LocalContainerEntityManagerFactoryBean
      entityManagerFactory(EntityManagerFactoryBuilder builder,
          MultiTenantConnectionProvider multiTenantConnectionProvider,
          CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver,
          @Qualifier("defaultDataSource") DataSource dataSource) {

    Map<String, Object> hibernateProps =
      new HashMap<>(jpaProperties.getProperties());


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
  public CurrentTenantIdentifierResolver<String> currentCommunityIdentifierResolver() {
    return new CurrentTenantResolver();
  }

  @Bean(name = "liquibase")
  @DependsOn("dataSourceBasedCommunityConnectionProvider")
  public MultiTenantSpringLiquibase liquibase() {

    MultiTenantSpringLiquibase multiTenantSpringLiquibase =
        new MultiTenantSpringLiquibase();

    multitenancyProperties.getTenants()
      .forEach(tc -> multiTenantSpringLiquibase.addDataSource(
          DataSourceBuilder.create()
              .driverClassName(tc.getDriverClassName())
              .username(tc.getUsername())
              .password(tc.getPassword())
              .url(tc.getJdbcUrl())
              .putAll(tc.getDataSourceProperties())
              .build()
      ));

    multiTenantSpringLiquibase.setChangeLog(liquibaseChangelogUrl);
    multiTenantSpringLiquibase.setShouldRun(true);

    return multiTenantSpringLiquibase;
  }
}
