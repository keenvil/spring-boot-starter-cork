package com.keenvil.cork.multitenancy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * keenvil.multitenancy.datasource.post-process (07/11/2026): el pool de cada barrio pasa por los
 * BeanPostProcessors (asi lo instrumenta datasource-micrometer) como un bean llamado
 * {@code tenantDataSource_<barrio>}.
 */
class TenantDataSourcePostProcessingTest {

  @Test
  void tenantDataSourcesGoThroughTheBeanPostProcessors() {
    DataSource defaultRaw = mock(DataSource.class);
    DataSource wrapped = mock(DataSource.class);
    List<String> names = new ArrayList<>();
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        names.add(beanName);
        return bean instanceof DataSource ? wrapped : bean;
      }
    });
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default", Map.of("default", defaultRaw));

    new MultitenancyAutoConfiguration().tenantDataSourcePostProcessing(provider, beanFactory)
        .afterSingletonsInstantiated();

    assertSame(wrapped, provider.selectAnyDataSource());
    assertSame(defaultRaw, provider.getDefaultDataSource());
    assertEquals(List.of("tenantDataSource_default"), names);
  }

  @Test
  void aPostProcessorThatReturnsSomethingElseKeepsTheRawDataSource() {
    DataSource defaultRaw = mock(DataSource.class);
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        return "no soy un DataSource";
      }
    });
    DataSourceBasedCommunityConnectionProvider provider =
        new DataSourceBasedCommunityConnectionProvider("default", Map.of("default", defaultRaw));

    new MultitenancyAutoConfiguration().tenantDataSourcePostProcessing(provider, beanFactory)
        .afterSingletonsInstantiated();

    assertSame(defaultRaw, provider.selectAnyDataSource());
  }
}
