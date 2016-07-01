package com.keenvil.email.service;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

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
  }

  /**
   * Send a Text Email using SES as the mail sender.
   * @param from The address to send the message from
   * @param to The address to send the message to
   * @param subject The subject of the message
   * @param text The text body of the message
   */
  public void sendTextEmail(final String from, final String[] to,
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
  public void sendHTMLEmail(final String from, final String[] to,
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
   * Sends either a text or HTML email using AWS SES
   * @param from the email address that is sending the email
   * @param to the email address to send the email to
   * @param subject the email subject
   * @param content the email content in a String format.
   *                It can either be text or HTML.
   *                If sending HTML content, isHTML must be true
   * @param isHTML A boolean indicating if the content is HTML or plain text.
   */
  private void sendEmail(final String from, final String[] to,
                         final String subject, final String content,
                         final boolean isHTML){

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
