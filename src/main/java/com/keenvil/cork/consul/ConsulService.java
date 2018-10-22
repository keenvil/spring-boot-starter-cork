package com.keenvil.cork.consul;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keenvil.cork.error.KeenvilApiException.UnprocessedEntity;
import com.keenvil.cork.multitenancy.DataSourceBuilder;
import com.netflix.config.ConcurrentCompositeConfiguration;;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.config.FixedDelayPollingScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.keenvil.cork.consul.Properties.*;
import static com.netflix.config.ConfigurationManager.install;
import static com.netflix.config.ConfigurationManager.isConfigurationInstalled;

/**
 * Consul Service
 * <p>this class initial {@link ConsulConfiguration} and is responsible for
 * returning the properties of archaius
 * </p>
 */
@Service
public class ConsulService {

  private static Logger log = LoggerFactory.getLogger(
      ConsulService.class.getName());

  /**
   * INITIAL_DELAY_MILLIS the time to delay first execution
   */
  private static final int INITIAL_DELAY_MILLIS = 2000;
  /**
   * DELAY_MILLIS the delay between the termination of one
   * execution and the commencement of the next
   */
  private static final int DELAY_MILLIS = 60000;
  /**
   * IGNORE_DELETES_FROM_SOURCE if this is false then delete value of
   * properties if  the value current is deleted in consul Server
   */
  private static final boolean IGNORE_DELETES_FROM_SOURCE = false;
  private String endPointPropertiesRequest;


  public ConsulService(String endPointPropertiesRequest)
      throws ConsulServiceException {
    this.endPointPropertiesRequest = endPointPropertiesRequest;
      installConfig();
      log.error("Error in ConsulService Configuration.");


  }

  public DataSource getDatasource(String tenantId) {
    log.info("Getting tenant datasource for: [{}]", tenantId);
    final DynamicStringProperty datasourceProperties =
        DynamicPropertyFactory.getInstance().getStringProperty(
            tenantId +  MYSQL,
            "");

    if (!datasourceProperties.get().isEmpty()) {
      return tenantDatasource(datasourceProperties.get());
    }
    log.error("no tenant datasource configuration for: [{}]" + tenantId);
    throw new UnprocessedEntity("no tenant configuration for: " + tenantId);
  }

  public Map<String, Object> getRabbitPropertiesConnectionFactory(String tenantId) {
    log.info("Getting rabbit connection for tenant: [{}]", tenantId);
    final DynamicStringProperty rabbitProperties =
        DynamicPropertyFactory.getInstance().getStringProperty(
            tenantId + RABBIT,
            "");

    if (!rabbitProperties.get().isEmpty()) {
      return getPropertiesConnectionFactory(rabbitProperties.get());
    }
    log.error("no rabbit configuration for tenant: [{}]" + tenantId);
    throw new UnprocessedEntity("no rabbit connection factory for: " + tenantId);
  }

  public String getMongoPropertiesConnectionFactory(String tenantId) {
    log.info("Getting mongo connection for tenant: [{}]", tenantId);
    final DynamicStringProperty mongoProperties =
        DynamicPropertyFactory.getInstance().getStringProperty(
            tenantId + MONGO,
            "");
    if (!mongoProperties.get().isEmpty()) {
      return (String) getPropertiesConnectionFactory(
          mongoProperties.get()).get("database");
    }
    log.error("no mongo configuration for tenant: [{}]" + tenantId);
    throw new UnprocessedEntity("no mongo DB  connection factory for tenant: " + tenantId);
  }

  private void installConfig()  {
    if (!isConfigurationInstalled()) {
      ConsulConfiguration consulDatabasesConfiguration =
          new ConsulConfiguration(
              endPointPropertiesRequest + MYSQL);

      DynamicConfiguration dynamicConfigurationDatabases = new DynamicConfiguration(
          consulDatabasesConfiguration, new FixedDelayPollingScheduler(
          INITIAL_DELAY_MILLIS, DELAY_MILLIS, IGNORE_DELETES_FROM_SOURCE));

      ConsulConfiguration consulQueuesConfiguration =
          new ConsulConfiguration(
              endPointPropertiesRequest + RABBIT);

      DynamicConfiguration dynamicConfigurationQueues = new DynamicConfiguration(
          consulQueuesConfiguration, new FixedDelayPollingScheduler(
          INITIAL_DELAY_MILLIS, DELAY_MILLIS, IGNORE_DELETES_FROM_SOURCE));

      ConsulConfiguration consulMongoConfiguration =
          new ConsulConfiguration(
              endPointPropertiesRequest + MONGO);

      DynamicConfiguration dynamicConfigurationMongo = new DynamicConfiguration(
          consulMongoConfiguration, new FixedDelayPollingScheduler(
          INITIAL_DELAY_MILLIS, DELAY_MILLIS, IGNORE_DELETES_FROM_SOURCE));

      ConcurrentCompositeConfiguration finalConfig =
          new ConcurrentCompositeConfiguration(dynamicConfigurationDatabases);
      finalConfig.addConfiguration(
          dynamicConfigurationDatabases, "mysql");
      finalConfig.addConfiguration(
          dynamicConfigurationQueues, "rabbit");
      finalConfig.addConfiguration(
          dynamicConfigurationMongo, "mongo");

      install(finalConfig);
    }
  }

  private Map<String, Object> getPropertiesConnectionFactory(
      String stringProperties) {
    Object jsonDatasource = new JsonParser().parse(stringProperties);
    return jsonToMap((JsonElement) jsonDatasource);
  }

  private DataSource tenantDatasource(String stringDatasource) {
    Map<String, Object> mapProperties;

    Object jsonDatasource = new JsonParser().parse(stringDatasource);
    mapProperties = jsonToMap((JsonElement) jsonDatasource);

    return DataSourceBuilder.create().driverClassName(
        (String) mapProperties.get("driverClassName"))
        .username((String) mapProperties.get("username"))
        .password(
            (String) mapProperties.get("password"))
        .url(
            (String) mapProperties.get("url"))
        .build();
  }

  private Map<String, Object> jsonToMap(JsonElement jsonElement) {
    Map<String, Object> tenantProperties = new HashMap<>();

    if (jsonElement.isJsonObject()) {
      Set<Map.Entry<String, JsonElement>> setValues = (
          (JsonObject) jsonElement).entrySet();

      if (setValues != null) {
        for (Map.Entry<String, JsonElement> mapValue : setValues) {
          tenantProperties.put(mapValue.getKey(),
              mapValue.getValue().getAsString());
        }
      }
    }
    return tenantProperties;
  }
}