package com.keenvil.autoconfiguration;

import com.keenvil.security.jwt.JwtService;
import com.keenvil.security.service.PlatformSecurityService;
import com.keenvil.mail.service.EmailService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.cloud.aws.mail.simplemail.SimpleEmailServiceJavaMailSender;
import org.springframework.mail.javamail.JavaMailSender;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;


@Configuration
public class KeenvilAutoConfiguration {

  @Value("${cloud.aws.credentials.accessKey}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secretKey}")
  private String secretKey;

  @Value("${cloud.aws.region}")
  private String region;

  @Bean
  @ConditionalOnMissingBean
  public BasicAWSCredentials basicAWSCredentials() {
    return new BasicAWSCredentials(accessKey, secretKey);
  }

  @Bean
  @ConditionalOnMissingBean
  AmazonSimpleEmailService amazonSimpleEmailService(AWSCredentials credentials) {
    AmazonSimpleEmailService amazonSimpleEmailService = new AmazonSimpleEmailServiceClient(credentials);
    amazonSimpleEmailService.setRegion(Region.getRegion(Regions.US_EAST_1));
    return amazonSimpleEmailService;
  }

  @Bean
  @ConditionalOnMissingBean
  JavaMailSender mailSender(AmazonSimpleEmailService amazonSimpleEmailService) {
    return new SimpleEmailServiceJavaMailSender(amazonSimpleEmailService);
  }

  @Bean
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
