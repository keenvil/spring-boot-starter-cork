package com.keenvil.autoconfiguration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;

import com.keenvil.mail.service.EmailService;
import com.keenvil.web.security.jwt.JwtService;
import com.keenvil.web.security.service.PlatformSecurityService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.aws.mail.simplemail.SimpleEmailServiceJavaMailSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class KeenvilAutoConfiguration {

  @Value("${access_key}")
  private String accessKey;

  @Value("${secret_key}")
  private String secretKey;

  @Value("${cloud.aws.region}")
  private String region;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigIn() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  /** Basic AWS Credentials.
   * @return AWS bean credentials.
   */
  @Bean
  public AWSCredentials basicAWSCredentials() {
    return new BasicAWSCredentials(accessKey, secretKey);
  }

  /** Simple email service.
   * @param credentials credentials.
   * @return email service.
   */
  @Bean
  public AmazonSimpleEmailService amazonSimpleEmailService(
      AWSCredentials credentials) {
    AmazonSimpleEmailService amazonSimpleEmailService = 
        new AmazonSimpleEmailServiceClient(credentials);
    amazonSimpleEmailService.setRegion(Region.getRegion(Regions
        .valueOf(region)));
    return amazonSimpleEmailService;
  }

  @Bean
  public JavaMailSender mailSender(
      AmazonSimpleEmailService amazonSimpleEmailService) {
    return new SimpleEmailServiceJavaMailSender(amazonSimpleEmailService);
  }

  @Bean
  @ConditionalOnMissingBean
  public EmailService emailService() {
    return new EmailService();
  }

  @Bean(name = "securityService")
  @ConditionalOnMissingBean
  public PlatformSecurityService securityService() {
    return new PlatformSecurityService();
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtService jwtService() {
    return new JwtService();
  }
}
