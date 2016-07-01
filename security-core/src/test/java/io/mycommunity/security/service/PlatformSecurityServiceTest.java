package io.mycommunity.security.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashSet;

import org.easymock.TestSubject;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.google.common.collect.Sets;

import io.mycommunity.security.service.JwtService.JwtUser;

public class PlatformSecurityServiceTest {

  @TestSubject
  private PlatformSecurityService service = new PlatformSecurityService();

  @Test
  public void hasRoleInCommunity() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("KEENVIL");
    replay(attributes);

    RequestContextHolder.setRequestAttributes(attributes);
    JwtUser jwtUser =
        new JwtUser(1L, "admin@keenvil.com", Sets.newHashSet("ADMIN_KEENVIL"));
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, "ADMIN");

    assertTrue(hasCommunityRole);
    verify(attributes);
  }

  @Test
  public void hasntRoleInCommunity() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("KEENVIL");
    replay(attributes);

    RequestContextHolder.setRequestAttributes(attributes);
    HashSet<String> roles = Sets.newHashSet("RESIDENT_KEENVIL", "ADMIN_MYCO");
    JwtUser jwtUser =
        new JwtUser(1L, "admin@keenvil.com", roles);
    boolean hasCommunityRole = service.hasRoleInCommunity(jwtUser, "ADMIN");

    assertFalse(hasCommunityRole);
    verify(attributes);
  }
}
