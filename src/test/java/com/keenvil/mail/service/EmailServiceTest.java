package com.keenvil.mail.service;

import com.keenvil.autoconfiguration.KeenvilAutoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KeenvilAutoConfiguration.class)
public class EmailServiceTest {

  @Autowired
  private EmailService service;

  @Test
  public void sendTextMessage_mailSent() throws Exception {

    String[] to = {"no-reply@keenvil.com"};

    service.sendTextEmail("info@keenvil.com", to,
        "Test", "This is a test email");
  }

}