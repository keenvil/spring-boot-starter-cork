package com.keenvil.cork.consul;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.keenvil.cork.error.KeenvilApiException.UnprocessedEntity;
import com.keenvil.cork.multitenancy.DataSourceBuilder;
import com.netflix.config.*;
import org.apache.commons.configuration.ConfigurationException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.netflix.config.ConfigurationManager.isConfigurationInstalled;

/**
 * Consul Service
 * <p>this class initial {@link ConsulConfiguration} and is responsible for
 * returning the properties of archaius
 * </p>
 */
@Service
public class ConsulService {

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
    try {
      installConfig();
    } catch (ConfigurationException e) {
      throw new ConsulServiceException("Error in configuration ConsulService: "
          + e.getMessage());
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