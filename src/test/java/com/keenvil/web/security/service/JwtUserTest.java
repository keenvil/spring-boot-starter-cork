package com.keenvil.web.security.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.keenvil.web.security.jwt.JwtService;
import com.keenvil.web.security.jwt.JwtService.JwtUser;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JwtUserTest {

  @Test
  public void hasRoleInCommunity() throws Exception {
    Set<String> roles = new HashSet<String>();
    Collections.addAll(roles, "USER_default", "ADMIN_someCommunity");

    JwtUser user = new JwtService.JwtUser(1L, "admin", roles);
    assertTrue(user.hasRoleInCommunity("USER", "default"));
    assertTrue(user.hasRoleInCommunity("ADMIN", "someCommunity"));
    assertFalse(user.hasRoleInCommunity("USER", "someCommunity"));
    assertFalse(user.hasRoleInCommunity("ADMIN", "default"));
  }
}
