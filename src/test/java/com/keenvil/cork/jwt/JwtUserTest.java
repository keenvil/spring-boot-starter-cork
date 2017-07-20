package com.keenvil.cork.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class JwtUserTest {

  @Test
  public void hasRoleInCommunity() throws Exception {
    Set<String> roles = new HashSet<String>();
    Collections.addAll(roles, "USER_default", "ADMIN_someCommunity");

    JwtUser user = new JwtUser(1L,  "Joe", "Average", "B-52",
        "admin", roles);
    assertTrue(user.hasRoleInCommunity("USER", "default"));
    assertTrue(user.hasRoleInCommunity("ADMIN", "someCommunity"));
    assertFalse(user.hasRoleInCommunity("USER", "someCommunity"));
    assertFalse(user.hasRoleInCommunity("ADMIN", "default"));

    user = new JwtUser(1L,  "Joe", "Average", "B-52",
        "admin", roles, "uri");
    assertThat(user.getAvatarUri(), is("uri"));
  }
}
