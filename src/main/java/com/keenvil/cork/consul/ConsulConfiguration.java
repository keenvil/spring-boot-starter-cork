package com.keenvil.cork.consul;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.transport.TransportException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonParser;
import com.netflix.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.netflix.config.PollResult.createFull;

import java.util.*;

/**
 * Consul Configuration
 * <p>
 * <p>this class define configuration for Consul and Archaius configuration</p>
 */
public class ConsulConfiguration implements PolledConfigurationSource {

  private static Logger log = LoggerFactory.getLogger(
      ConsulConfiguration.class.getName());

  private ConsulClient client;
  private String endPointKey;

  public ConsulConfiguration(String endPointKey) {
    this.endPointKey = endPointKey;
  }

  @Override
  public PollResult poll(boolean initial, Object checkPoint) throws TransportException {
    if (client != null || createClient()) {
      try {
        Response<List<GetValue>> kvValues = client.getKVValues(endPointKey);
        return PollResult.createFull(responseToMap(kvValues));
      } catch (TransportException e) {
        log.error("Service not available on host review connection to consul. ");
        throw new TransportException(e);
      }
    }
    return initial ? createFull(Collections.EMPTY_MAP) : null;
  }

  private boolean createClient() {
    String host = ConfigurationManager.getConfigInstance()
        .getString("application.consul.host");
    Integer port = ConfigurationManager.getConfigInstance()
        .getInteger("application.consul.port", 0);

    if (host != null && port != 0) {
      this.client = new ConsulClient(host, port);
      return true;
    }
    throw new ConsulException("can not initialize client Consul review " +
        "properties");
  }

  private Map<String, Object> responseToMap(
      Response<List<GetValue>> listResponse) {
    Map<String, Object> map = new HashMap<>();

    if (listResponse.getValue() != null && !listResponse.getValue().isEmpty()) {
      for (GetValue value : listResponse.getValue()) {
        if (!value.getKey().endsWith("/")) {
          Object jsonDecoded = new JsonParser().parse(
              new String(BaseEncoding.base64().
                  decode(value.getValue()))).getAsJsonObject();
          String key = value.getKey().substring(
              value.getKey().lastIndexOf("/") + 1);
          map.put(key, jsonDecoded);
        }
      }
    }
    return map;
  }
}