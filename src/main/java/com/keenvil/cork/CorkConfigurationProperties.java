package com.keenvil.cork;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.keenvil.cork.security.QrServiceProperties;

/**
 *  Cork general configuration properties.
 *  
 *  <p>Common Cork properties are exposed here.</p>
 */
@ConfigurationProperties(prefix = "keenvil.cork")
public class CorkConfigurationProperties {

  public enum CommunityResolverStrategy {
    REQUEST_ATTRIBUTE,
    URL
  }

  private CommunityResolverStrategy communityResolver;

  private QrServiceProperties qrGenerator;

  public CorkConfigurationProperties() { }

  public CommunityResolverStrategy getCommunityResolver() {
    return communityResolver;
  }

  public void setCommunityResolver(
      CommunityResolverStrategy theCommunityResolver) {
    communityResolver = theCommunityResolver;
  }

  public QrServiceProperties getQrGenerator() {
    return qrGenerator;
  }

  public void setQrGenerator(QrServiceProperties theqrGenerator) {
    qrGenerator = theqrGenerator;
  }
}
