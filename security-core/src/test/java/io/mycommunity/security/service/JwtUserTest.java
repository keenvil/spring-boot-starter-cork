package io.mycommunity.security.service;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import com.google.common.collect.Sets;

import io.mycommunity.security.service.JwtService.JwtUser;

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
