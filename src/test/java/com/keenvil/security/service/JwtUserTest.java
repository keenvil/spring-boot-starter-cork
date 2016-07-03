package com.keenvil.security.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import com.keenvil.security.jwt.JwtService.JwtUser;

import org.junit.Test;

import java.util.HashSet;

public class JwtUserTest {

  @Test
  public void hasRoleInCommunity() {
    HashSet<String> roles = Sets.newHashSet("ADMIN_KEENVIL");
    JwtUser jwtUser = new JwtUser(1L, "admin@keenvil.com", roles);
    boolean hasRole = jwtUser.hasRoleInCommunity("ADMIN", "KEENVIL");
    assertTrue(hasRole);
  }

  @Test
  public void hasntRoleInCommunity() {
    HashSet<String> roles = Sets.newHashSet("ADMIN_MYCO", "RESIDENT_KEENVIL");
    JwtUser jwtUser = new JwtUser(1L, "admin@keenvil.com", roles);
    boolean hasRole = jwtUser.hasRoleInCommunity("ADMIN", "KEENVIL");
    assertFalse(hasRole);
  }
}
