package com.keenvil.email.service;

import com.keenvil.email.configuration.EmailServiceConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Created by Franco on 6/30/16.
 */
@ContextConfiguration(classes=EmailServiceConfiguration.class, loader=AnnotationConfigContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class EmailServiceTest {

  private EmailService service = new EmailService();

  @Test
  public void sendTextMessage_mailSent() throws Exception {

    String[] to = {"franco@keenvil.com"};

    service.sendTextEmail("info@keenvil.com", to,
        "Test", "This is a test email");
  }

}