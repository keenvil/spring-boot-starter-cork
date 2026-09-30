package com.keenvil.cork.multitenancy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariDataSource;

class MultiTenantSpringLiquibaseTest {

  @Test
  void closesEachDataSourceAfterRunningSoItsPoolDoesNotStayOpen() throws Exception {
    HikariDataSource first = mock(HikariDataSource.class);
    HikariDataSource second = mock(HikariDataSource.class);
    MultiTenantSpringLiquibase liquibase = new MultiTenantSpringLiquibase();
    liquibase.setShouldRun(false);
    liquibase.addDataSource(first);
    liquibase.addDataSource(second);

    liquibase.afterPropertiesSet();

    verify(first).close();
    verify(second).close();
  }
}
