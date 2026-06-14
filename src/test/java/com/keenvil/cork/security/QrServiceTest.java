package com.keenvil.cork.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.keenvil.cork.CorkConfigurationProperties;

@ExtendWith(EasyMockExtension.class)
public class QrServiceTest {

  @TestSubject
  private QrService service = new QrService();

  @Mock
  private CorkConfigurationProperties properties;

  @Test
  public void generatQr() throws Exception {
    QrServiceProperties qrProperties =
        new QrServiceProperties("PNG", 150, 150, "UTF-8");
    expect(properties.getQrGenerator()).andReturn(qrProperties);
    replay(properties);

    String qr = service.generateQr("123456789");
    assertThat(qr, notNullValue());
    verify(properties);
  }
}
