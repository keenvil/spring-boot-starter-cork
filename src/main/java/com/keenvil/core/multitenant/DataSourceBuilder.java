package com.keenvil.core.multitenant;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.util.ClassUtils;

/**
 * Most of this class was borrowed from 
 * {@link org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder}.
 * 
 * <p>Convenience class for building a {@link DataSource} with common
 * implementations andproperties. If Tomcat, HikariCP or Commons DBCP are on
 * the classpath one of them will be selected (in that order with Tomcat
 * first). In the interest of a uniform interface, and so that there can be a
 * fallback to an embedded database if one can be detected on the classpath,
 * only a small set of common configuration properties are supported. To
 * inject additional properties into the result you can downcast it, or use
 * {@code @ConfigurationProperties}.</p>
 */
public class DataSourceBuilder {

  private static final String[] DATA_SOURCE_TYPE_NAMES = new String[] {
      "org.apache.tomcat.jdbc.pool.DataSource",
      "com.zaxxer.hikari.HikariDataSource",
      "org.apache.commons.dbcp.BasicDataSource",
      "org.apache.commons.dbcp2.BasicDataSource" };

  private Class<? extends DataSource> type;

  private ClassLoader classLoader;

  private Map<String, String> properties = new HashMap<String, String>();

  public static DataSourceBuilder create() {
    return new DataSourceBuilder(null);
  }

  public static DataSourceBuilder create(ClassLoader classLoader) {
    return new DataSourceBuilder(classLoader);
  }

  public DataSourceBuilder(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Builds a Data source.
   * @return Data source.
   */
  public DataSource build() {
    Class<? extends DataSource> type = getType();
    DataSource result = BeanUtils.instantiate(type);
    maybeGetDriverClassName();
    bind(result);
    return result;
  }

  private void maybeGetDriverClassName() {
    if (!this.properties.containsKey("driverClassName")
        && this.properties.containsKey("url")) {
      String url = this.properties.get("url");
      String driverClass = DatabaseDriver.fromJdbcUrl(url).getDriverClassName();
      this.properties.put("driverClassName", driverClass);
    }
  }

  private void bind(DataSource result) {
    MutablePropertyValues properties =
        new MutablePropertyValues(this.properties);
    new RelaxedDataBinder(result).withAlias("url", "jdbcUrl")
        .withAlias("username", "user").bind(properties);
  }

  public DataSourceBuilder type(Class<? extends DataSource> type) {
    this.type = type;
    return this;
  }

  public DataSourceBuilder url(String url) {
    this.properties.put("url", url);
    return this;
  }

  public DataSourceBuilder driverClassName(String driverClassName) {
    this.properties.put("driverClassName", driverClassName);
    return this;
  }

  public DataSourceBuilder username(String username) {
    this.properties.put("username", username);
    return this;
  }

  public DataSourceBuilder password(String password) {
    this.properties.put("password", password);
    return this;
  }

  public DataSourceBuilder putAll(final Map<String, String> theProperties) {
    properties.putAll(theProperties);
    return this;
  }

  /**
   * Finds the data source type.
   * @return Data source.
   */
  @SuppressWarnings("unchecked")
  public Class<? extends DataSource> findType() {
    if (this.type != null) {
      return this.type;
    }
    for (String name : DATA_SOURCE_TYPE_NAMES) {
      try {
        return (Class<? extends DataSource>) ClassUtils.forName(name,
            this.classLoader);
      } catch (Exception ex) {
        // Swallow and continue
      }
    }
    return null;
  }

  private Class<? extends DataSource> getType() {
    Class<? extends DataSource> type = findType();
    if (type != null) {
      return type;
    }
    throw new IllegalStateException("No supported DataSource type found");
  }

}
