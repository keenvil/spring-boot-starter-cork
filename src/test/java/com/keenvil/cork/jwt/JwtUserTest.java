package com.keenvil.cork.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.junit.Test;

public class JwtUserTest {

  @Test
  public void hasRoleInCommunity() throws Exception {
    Set<String> roles = new HashSet<>();
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

    Set<String> singleRole = new HashSet<>();
    Collections.addAll(singleRole, "TENANT_default");
    JwtUser userSingle = new JwtUser(1L, "Victor", "Average", "B-52",
        "vic", singleRole);
    assertTrue(userSingle.hasRoleInCommunity(Collections.singletonList("TENANT"), "default"));
    assertFalse(userSingle.hasRoleInCommunity(Collections.singletonList("OWNER"), "default"));

    Set<String> twoRoles = new HashSet<>();
    Collections.addAll(twoRoles, "TENANT_default", "OWNER_default");
    JwtUser userTowRoles = new JwtUser(1L, "Victor", "Average", "B-52",
        "vic", twoRoles);
    assertTrue(userTowRoles.hasRoleInCommunity(Arrays.asList("TENANT", "OWNER"), "default"));
    assertFalse(userTowRoles.hasRoleInCommunity(Arrays.asList("SECURITY", "GUARD"), "default"));


    user = new JwtUser(1L, "Joe", "Average", "B-52",
        "admin", roles, "uri");
    assertThat(user.getAvatarUri(), is("uri"));
  }
}
