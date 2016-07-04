package com.keenvil.mail.service;

import org.junit.Test;
import org.easymock.TestSubject;

/**
 * Created by Franco on 6/30/16.
 */
public class EmailServiceTest {

  @TestSubject
  private EmailService service = new EmailService();

  @Test
  public void sendTextMessage_mailSent() throws Exception {

    String[] to = {"franco@keenvil.com"};

    service.sendTextEmail("info@keenvil.com", to,
        "Test", "This is a test email");
  }

}