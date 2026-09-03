package com.keenvil.cork.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.keenvil.cork.consul.ConsulService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots a real Spring context with {@link MultitenancyAutoConfiguration} active,
 * the exact scenario that used to fail under Boot 3 with
 * "Requested bean is currently in creation: Is there an unresolvable circular
 * reference?" on {@code dataSourceBasedCommunityConnectionProvider} (a bean
 * produced by a {@code @Bean} method of this very class, previously wired back
 * into the class via {@code @Autowired} fields instead of method parameters).
 *
 * <p>No existing test in this module actually started this auto-configuration's
 * Spring context before, which is exactly why the regression shipped unnoticed
 * in crowd-api and guard-api.</p>
 */
@SpringBootTest(classes = MultitenancyAutoConfigurationTest.TestApp.class)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "keenvil.cork.multitenancy.tenants-strategy=SHARED-DB-SEPARATE-SCHEMAS",
    "keenvil.cork.multitenancy.tenants[0].name=primary",
    "keenvil.cork.multitenancy.tenants[0].jdbc-url=jdbc:h2:mem:cork-multitenancy-it;DB_CLOSE_DELAY=-1",
    "keenvil.cork.multitenancy.tenants[0].username=sa",
    "keenvil.cork.multitenancy.tenants[0].password=",
    "keenvil.cork.multitenancy.tenants[0].driver-class-name=org.h2.Driver",
    "keenvil.cork.multitenancy.tenants[0].default=true",
    "application.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "application.hibernate.hbm2ddl=none",
    "spring.liquibase.changeLog=classpath:/db/changelog/empty-changelog.xml",
    "spring.liquibase.enabled=true",
})
class MultitenancyAutoConfigurationTest {

  @org.springframework.beans.factory.annotation.Autowired
  private EntityManagerFactory entityManagerFactory;

  @Test
  void contextStartsWithoutCircularReferenceAndBuildsTheEntityManagerFactory() {
    assertThat(entityManagerFactory, is(notNullValue()));
    assertThat(entityManagerFactory.isOpen(), is(true));
  }

  @Configuration
  @EnableAutoConfiguration
  @EnableMultitenancy(basePackages = "com.keenvil.cork.multitenancy.testsupport")
  static class TestApp {

    // DataSourceBasedCommunityConnectionProvider needs a real ConsulService in
    // production (an external dependency unrelated to this test's purpose); a
    // mock is enough here since nothing in the circular-reference fix under
    // test actually calls it.
    @Bean
    ConsulService consulService() {
      return Mockito.mock(ConsulService.class);
    }
  }
}
