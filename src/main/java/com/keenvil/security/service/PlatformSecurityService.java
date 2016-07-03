package com.keenvil.security.service;

import com.keenvil.security.jwt.JwtService.JwtUser;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Security services to grant access to resources according to user principals.
 */
@Service
public class PlatformSecurityService {

  /** Returns {@code true} if the given user has the given role in any of
   * his/her communities.
   *
   * @param jwtUser user to validate.
   * @param role role to validate.
   * @return whether the given user has the given role in any of his/her
   *      communities.
   */
  public boolean hasRoleInCommunity(final JwtUser jwtUser, String role) {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    String communityId = (String) attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST);
    return jwtUser.hasRoleInCommunity(role, communityId);
  }
}
