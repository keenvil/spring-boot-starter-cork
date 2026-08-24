package com.keenvil.cork.consul;


import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.common.io.BaseEncoding;
import com.keenvil.cork.error.KeenvilApiException.UnprocessedEntity;
import com.keenvil.cork.multitenancy.DataSourceBuilder;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(EasyMockExtension.class)
public class ConsulConfigurationTest {

  private static final String END_POINT = "test-consul";
  private static final String KEY = "test";

  @Mock
  private ConsulClient consulClient;

  @Mock
  private ConsulService consulService;

  @Test
  public void testGetDatasourceFromConsul() throws Exception {
    expect(consulService.getDatasource(KEY)).andReturn(
        DataSourceBuilder.create().build());
    replay(consulService);

    assertNotNull(consulService.getDatasource(KEY));
    verify(consulService);
  }

  @Test
  public void testGetDatasourceConsulClient(){
    String json ="{" +
        "  \"identifier\":\"primary\",\n" +
        "  \"diverClassName\": \"com.mysql.jdbc.Driver\",\n" +
        "  \"username\": \"root\",\n" +
        "  \"password\": \"root\",\n" +
        "  \"url\": \"jdbc:mysql://localhost:3306/guard-primary\"\n" +
        "}";
    Response<List<GetValue>> setResponse;

    List<GetValue> arrayList = new ArrayList<>();
    GetValue intValue = new GetValue();
    intValue.setKey(END_POINT + "/" + KEY);
    intValue.setValue(BaseEncoding.base64().encode(json.getBytes()));
    setResponse = new Response<>(arrayList, 0l,
        false, 0l);
    expect(consulClient.getKVValues(KEY)).andReturn(setResponse);
    replay(consulClient);
    Response<List<GetValue>> getResponse = consulClient.getKVValues(KEY);

    assertTrue(setResponse.getValue().containsAll(
        getResponse.getValue()) && getResponse.getValue().containsAll(
            setResponse.getValue()));

    verify(consulClient);

  }

  @Test
  public void testGetDatasourceException() throws Exception {
    expect(consulService.getDatasource(KEY)).andThrow(
        new UnprocessedEntity("no tenant configuration for: test"));
    replay(consulService);

    assertThrows(UnprocessedEntity.class, () -> consulService.getDatasource(KEY));
    verify(consulService);
  }
}
