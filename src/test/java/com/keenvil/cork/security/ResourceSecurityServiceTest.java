package com.keenvil.cork.security;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.keenvil.cork.RequestAttributeCommunityResolver;
import com.keenvil.cork.jwt.JwtUser;
import com.keenvil.cork.security.ResourceSecurityService;

import org.easymock.TestSubject;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ResourceSecurityServiceTest {

  @TestSubject
  private ResourceSecurityService service =
      new ResourceSecurityService(
          new RequestAttributeCommunityResolver());

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
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, Collections.singletonList("ADMIN"));

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
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, Collections.singletonList("ADMIN"));

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
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, Arrays.asList("TENANT", "CORESIDENT", "OWNER"));

    assertTrue(hasCommunityRole);
    verify(attributes);
  }
}
