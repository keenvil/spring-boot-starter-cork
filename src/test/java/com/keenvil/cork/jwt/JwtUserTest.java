package com.keenvil.cork.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class JwtUserTest {

  @Test
  public void hasRoleInCommunity() throws Exception {
    Set<String> roles = new HashSet<String>();
    Collections.addAll(roles, "TENANT_default", "CORESIDENT_default",
        "OWNER_default", "ADMIN_someCommunity", "GUARD_someCommunity", "SECURITY_someCommunity");

    JwtUser user = new JwtUser(1L, "Joe", "Average", "B-52",
        "admin", roles);
    assertTrue(user.hasRoleInCommunity(Arrays.asList("TENANT", "CORESIDENT", "OWNER"), "default"));
    assertTrue(user.hasRoleInCommunity(Arrays.asList("GUARD", "SECURITY"), "someCommunity"));
    assertTrue(user.hasRoleInCommunity(Collections.singletonList("ADMIN"), "someCommunity"));
    assertFalse(user.hasRoleInCommunity(Arrays.asList("TENANT", "CORESIDENT", "OWNER"), "someCommunity"));
    assertFalse(user.hasRoleInCommunity(Collections.singletonList("ADMIN"), "default"));
    assertFalse(user.hasRoleInCommunity(Arrays.asList("GUARD", "SECURITY"), "default"));


    user = new JwtUser(1L, "Joe", "Average", "B-52",
        "admin", roles, "uri");
    assertThat(user.getAvatarUri(), is("uri"));
  }
}
