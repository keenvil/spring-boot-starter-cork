package com.keenvil.cork;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

  public CorkConfigurationProperties() { }

  public CommunityResolverStrategy getCommunityResolver() {
    return communityResolver;
  }

  public void setCommunityResolver(
      CommunityResolverStrategy theCommunityResolver) {
    communityResolver = theCommunityResolver;
  }
}
