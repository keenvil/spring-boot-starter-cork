package com.keenvil.email.service;

import static org.slf4j.LoggerFactory.getLogger;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import org.springframework.cloud.aws.mail.simplemail.SimpleEmailServiceJavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;

/**
 * Email Service to send mails using AWS SES server
 * 
 */
@Service
public class EmailService {

  private static Logger log = getLogger(EmailService.class);

  @Autowired
  private JavaMailSender mailSender;

  public EmailService() {
/*
    BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAIUHILB7UDGS4SFXA", "AqQc2RBqih04IvMTZ6ITO40hltCF7YSMm9wmeTkaD+fu");
    AmazonSimpleEmailService amazonSimpleEmailService = new AmazonSimpleEmailServiceClient();
    amazonSimpleEmailService.setRegion(Region.getRegion(Regions.US_EAST_1));
    mailSender = new SimpleEmailServiceJavaMailSender(amazonSimpleEmailService);
 */
  }

  /**
   * Send a Text Email using SES as the mail sender.
   * @param from The address to send the message from
   * @param to The address to send the message to
   * @param subject The subject of the message
   * @param text The text body of the message
   */
  public void sendTextEmail(final String from, final String to,
      final String subject, final String text) {

    log.trace("Entering sendTextMessage.");

    Validate.notEmpty(from);
    Validate.notEmpty(to);
    Validate.notEmpty(subject);
    Validate.notEmpty(text);

    sendEmail(from, to, subject, text, false);

    log.trace("Leaving sendTextMessage.");
  }

  /**
   * Send an HTML Email using SES as the mail sender.
   * @param from The address to send the message from
   * @param to The address to send the message to
   * @param subject The subject of the message
   * @param HTMLContent The HTML content in a string format
   */
  public void sendHTMLEmail(final String from, final String to,
                              final String subject, final String HTMLContent) {

    log.trace("Entering sendHTMLMessage.");

    Validate.notEmpty(from);
    Validate.notEmpty(to);
    Validate.notEmpty(subject);
    Validate.notEmpty(HTMLContent);

    sendEmail(from, to, subject, HTMLContent, true);

    log.trace("Leaving sendHTMLMessage.");
  }

  /**
   *
   * @param from
   * @param to
   * @param subject
   * @param content
   * @param isHTML
   */
  private void sendEmail(final String from, final String to,
                         final String subject, final String content, final boolean isHTML){

    MimeMessage message = mailSender.createMimeMessage();
    try {

      MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

      helper.setFrom(from);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content, isHTML);

    } catch(MessagingException me) {
      log.error("Error Building Text Email Message", me);
      throw new RuntimeException("Error Building Text Message", me);
    }

    mailSender.send(message);

  }
}
