package com.keenvil.cork.consul;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ConsulServiceCredentialTest {

  @Test
  void tenantValueWins() {
    assertEquals("tenant-user", ConsulService.credential("tenant-user", "svc_user"));
  }

  @Test
  void missingOrBlankTenantValueUsesServiceCredential() {
    assertEquals("svc_user", ConsulService.credential(null, "svc_user"));
    assertEquals("svc_user", ConsulService.credential("", "svc_user"));
    assertEquals("svc_user", ConsulService.credential("  ", "svc_user"));
  }

  @Test
  void noValueAnywhereStaysNull() {
    assertNull(ConsulService.credential(null, null));
  }
}
