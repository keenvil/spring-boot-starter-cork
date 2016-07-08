package com.keenvil.mail.service;

import com.keenvil.autoconfiguration.KeenvilAutoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.text.IsEmptyString.isEmptyString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = KeenvilAutoConfiguration.class)
public class EmailServiceTest {

  @Autowired
  private EmailService service;

  @Autowired
  private SpringTemplateEngine templateEngine;

  @Test
  public void sendTextMessage_mailSent() throws Exception {

    String[] to = {"no-reply@keenvil.com"};

    service.sendTextEmail("info@keenvil.com", to,
        "Test", "This is a test email");
  }

  @Test
  public void sendHTMLMessage_mailSent() throws Exception {

    // Prepare the evaluation context
    Context ctx = new Context(Locale.US);
    ctx.setVariable("name", "Andres");
    ctx.setVariable("subscriptionDate", new Date());
    ctx.setVariable("hobbies", Arrays.asList(
        "Cinema", "Sports", "Music"));

    // Create the HTML body using Thymeleaf
    String htmlContent = this.templateEngine.process(
        "test.html", ctx);

    assertThat(htmlContent, notNullValue());
    assertThat(htmlContent, not(isEmptyString()));
    

    String[] to = {"no-reply@keenvil.com"};

    service.sendHTMLEmail("info@keenvil.com", to,
        "Test", htmlContent);

  }

}