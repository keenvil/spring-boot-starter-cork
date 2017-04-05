package com.keenvil.web.security.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Obtains current requested Community Id from Request Attributes.
 */
@Component
public class RequestAttributeCommunityResolverHelper
    implements CummunityResolverHelper {

  /** Attribute name. */
  private static final String COMMUNITY_ID = "community-id";

  @Override
  public String resolve() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    String communityId = (String) attributes.getAttribute(COMMUNITY_ID,
            RequestAttributes.SCOPE_REQUEST);
    return communityId;
  }
}
