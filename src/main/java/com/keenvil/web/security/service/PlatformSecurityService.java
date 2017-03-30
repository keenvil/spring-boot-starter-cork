package com.keenvil.web.security.service;

import org.springframework.stereotype.Service;

import com.keenvil.web.security.jwt.JwtService.JwtUser;

/** Security services to grant access to resources according to user principals.
 */
@Service
public class PlatformSecurityService {

  private CummunityResolverHelper helper;

  public PlatformSecurityService(CummunityResolverHelper theHelper) {
    helper = theHelper;
  }

  /** Returns <code>true</code> if the given user has the given role or ADMIN
   *  role in any of his communities.
   *
   * @param jwtUser user to validate.
   * @param role role to validate.
   * @return whether the given user has the given role in any of his
   *     communities.
   */
  public boolean hasRoleInCommunity(final JwtUser jwtUser, String role) {
    String communityId = helper.resolve();
    return jwtUser.hasRoleInCommunity(role, communityId)
        || hasAdminInCommunity(jwtUser);
  }

  /** Returns <code>true</code> if the given user has ADMIN role in any of
   * his communities.
   * 
   * @param jwtUser user to validate.
   * @return whether the given user has ADMIN role in any of his communities.
   */
  public boolean hasAdminInCommunity(final JwtUser jwtUser) {
    String communityId = helper.resolve();
    return jwtUser.hasRoleInCommunity("ADMIN", communityId);
  }
}
