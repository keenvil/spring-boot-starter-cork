package com.keenvil.cork.security;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class QrServiceTest {

  @TestSubject
  private QrService service = new QrService();

  @Test
  public void generatQr() throws Exception {
    String qr = service.generateQr("123456789");
    assertThat(qr, notNullValue());
  }
}
