package com.keenvil.web.security.service;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import com.keenvil.web.security.jwt.JwtService.JwtUser;
import com.keenvil.web.security.service.PlatformSecurityService;

import org.easymock.TestSubject;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.HashSet;

public class PlatformSecurityServiceTest {

  @TestSubject
  private PlatformSecurityService service =
      new PlatformSecurityService(
          new RequestAttributeCommunityResolverHelper());

  @Test
  public void hasRoleInCommunity() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("KEENVIL");
    replay(attributes);

    RequestContextHolder.setRequestAttributes(attributes);
    JwtUser jwtUser =
        new JwtUser(1L,  "Joe", "Average", "B-52", "admin@keenvil.com",
            Sets.newHashSet("ADMIN_KEENVIL"));
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, "ADMIN");

    assertTrue(hasCommunityRole);
    verify(attributes);
  }

  @Test
  public void hasntRoleInCommunity() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("KEENVIL").times(2);
    replay(attributes);

    RequestContextHolder.setRequestAttributes(attributes);
    HashSet<String> roles = Sets.newHashSet("RESIDENT_KEENVIL", "ADMIN_MYCO");
    JwtUser jwtUser =
        new JwtUser(1L,  "Joe", "Average", "B-52", "admin@keenvil.com", roles);
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, "ADMIN");

    assertFalse(hasCommunityRole);
    verify(attributes);
  }

  @Test
  public void hasAdminRoleInCommunity() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("MYCO").times(2);
    replay(attributes);

    RequestContextHolder.setRequestAttributes(attributes);
    HashSet<String> roles = Sets.newHashSet("RESIDENT_KEENVIL", "ADMIN_MYCO");
    JwtUser jwtUser =
        new JwtUser(1L,  "Joe", "Average", "B-52", "admin@keenvil.com", roles);
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, "RESIDENT");

    assertTrue(hasCommunityRole);
    verify(attributes);
  }
}
