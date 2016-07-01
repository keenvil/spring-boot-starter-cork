package com.keenvil.email.configuration;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import org.springframework.cloud.aws.mail.simplemail.SimpleEmailServiceJavaMailSender;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailServiceConfiguration {

  @Value("${cloud.aws.credentials.accessKey}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secretKey}")
  private String secretKey;

  @Value("${cloud.aws.region}")
  private String region;

  @Bean
  public BasicAWSCredentials basicAWSCredentials() {
    return new BasicAWSCredentials(accessKey, secretKey);
  }

  @Bean
  AmazonSimpleEmailService amazonSimpleEmailService(AWSCredentials credentials) {
    AmazonSimpleEmailService amazonSimpleEmailService = new AmazonSimpleEmailServiceClient(credentials);
    amazonSimpleEmailService.setRegion(Region.getRegion(Regions.US_EAST_1));
    return amazonSimpleEmailService;
  }

  @Bean
  JavaMailSender mailSender(AmazonSimpleEmailService amazonSimpleEmailService) {
    return new SimpleEmailServiceJavaMailSender(amazonSimpleEmailService);
  }
}
