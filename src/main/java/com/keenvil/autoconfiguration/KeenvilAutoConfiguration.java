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

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.annotation.PropertySource;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

@Configuration
public class KeenvilAutoConfiguration {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigIn() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Value("${access_key}")
  private String accessKey;

  @Value("${secret_key}")
  private String secretKey;

  @Value("${cloud.aws.region}")
  private String region;

  @Bean
  public AWSCredentials basicAWSCredentials() {
    System.out.println("Access Key is: " + accessKey);
    System.out.println("Secret Key is: " + secretKey);
    return new BasicAWSCredentials(accessKey, secretKey);
  }

  @Bean
  public AmazonSimpleEmailService amazonSimpleEmailService(AWSCredentials credentials) {
    AmazonSimpleEmailService amazonSimpleEmailService = new AmazonSimpleEmailServiceClient(credentials);
    amazonSimpleEmailService.setRegion(Region.getRegion(Regions.valueOf(region)));
    return amazonSimpleEmailService;
  }

  @Bean
  public JavaMailSender mailSender(AmazonSimpleEmailService amazonSimpleEmailService) {
    return new SimpleEmailServiceJavaMailSender(amazonSimpleEmailService);
  }

  @Bean
  @ConditionalOnMissingBean
  public EmailService emailService() {
    return new EmailService();
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
