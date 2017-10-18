package com.keenvil.cork.consul;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keenvil.cork.error.KeenvilApiException.UnprocessedEntity;
import com.keenvil.cork.multitenancy.DataSourceBuilder;
import com.netflix.config.*;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.netflix.config.ConfigurationManager.isConfigurationInstalled;

/**
 * Consul Service
 * <p>
 * <p>this class initial {@link ConsulConfiguration} and is responsible for
 * returning the properties of archaius
 * </p>
 */
@Service
public class ConsulService {

  private String endPointPropertiesRequest;
  private static final Logger LOG = LoggerFactory.getLogger(
      ConsulService.class.getName());

  private static final int INITIAL_DELAY_MILLIS = -1;
  private static final int DELAY_MILLIS = 60000;
  private static final boolean IGNORE_DELETES_FROM_SOURCE = false;

  public ConsulService(String endPointPropertiesRequest) {
    this.endPointPropertiesRequest = endPointPropertiesRequest;
    try {
      installConfig();
    } catch (ConfigurationException e) {
      LOG.error("Error in configuration ConsulService: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public DataSource getDatasource(String tenantId) {
    final DynamicStringProperty datasourceProperties =
        DynamicPropertyFactory.getInstance().getStringProperty(tenantId,
            "");

    if (!datasourceProperties.get().isEmpty()) {
      return tenantDatasource(datasourceProperties.get());
    }
    throw new UnprocessedEntity("no tenant configuration for: " + tenantId);
  }

  private DataSource tenantDatasource(String stringDatasource) {
    Map<String, Object> mapProperties;

    Object jsonDatasource = new JsonParser().parse(stringDatasource);
    mapProperties = jsonToMap((JsonElement) jsonDatasource);

    return DataSourceBuilder.create().driverClassName(
        (String) mapProperties.get("diverClassName"))
        .username((String) mapProperties.get("username"))
        .password(
            (String) mapProperties.get("password"))
        .url(
            (String) mapProperties.get("url")).build();
  }

  private void installConfig() throws ConfigurationException {
    if (!isConfigurationInstalled()) {
      ConsulConfiguration consulConfiguration =
          new ConsulConfiguration(endPointPropertiesRequest);

      DynamicConfiguration dynamicConfiguration = new DynamicConfiguration(
          consulConfiguration, new FixedDelayPollingScheduler(
          INITIAL_DELAY_MILLIS, DELAY_MILLIS, IGNORE_DELETES_FROM_SOURCE));

      ConfigurationManager.install(dynamicConfiguration);
    }
  }

  private Map<String, Object> jsonToMap(JsonElement jsonElement) {
    Map<String, Object> properties = new HashMap<>();

    if (jsonElement.isJsonObject()) {
      Set<Map.Entry<String, JsonElement>> ens = (
          (JsonObject) jsonElement).entrySet();

      if (ens != null) {
        for (Map.Entry<String, JsonElement> en : ens) {
          properties.put(en.getKey(), en.getValue().getAsString());
        }
      }
    }
    return properties;
  }
}
