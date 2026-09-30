package com.keenvil.cork.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/** S10-SEC-05: metrica de tokens por algoritmo y flag para rechazar HS256 legacy. */
class JwtHs256MetricsTest {

  private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
  private final JwtService service = new JwtService();

  @BeforeEach
  void setUp() {
    Metrics.addRegistry(registry);
  }

  @AfterEach
  void tearDown() {
    Metrics.removeRegistry(registry);
    registry.close();
  }

  private String hs256Token() {
    // Sin jwt.rsa-private-key el servicio firma con la clave HMAC legacy (HS256).
    return service.generate("1", "Joe", "Average", "admin@keenvil.com", "B-52",
        Collections.singleton("USER"), new Date(System.currentTimeMillis() + 3_600_000));
  }

  private double count(String alg, String result) {
    return registry.find("cork.jwt.tokens").tag("alg", alg).tag("result", result).counters()
        .stream().mapToDouble(c -> c.count()).sum();
  }

  @Test
  void hs256IsAcceptedAndCountedByDefault() {
    JwtUser user = service.parse(hs256Token());

    assertNotNull(user);
    assertEquals(1.0, count("HS256", "accepted"));
    assertEquals(0.0, count("HS256", "rejected"));
    assertEquals("access", registry.find("cork.jwt.tokens").tag("alg", "HS256").counter().getId().getTag("type"));
    assertEquals("<1d", registry.find("cork.jwt.tokens").tag("alg", "HS256").counter().getId().getTag("age"));
  }

  @Test
  void hs256IsRejectedWhenFlagIsOn() {
    service.setRejectHs256ForTesting(true);
    String jwt = hs256Token();

    assertThrows(JwtInvalidTokenException.class, () -> service.parse(jwt));
    assertEquals(1.0, count("HS256", "rejected"));
    assertEquals(0.0, count("HS256", "accepted"));
  }

  @Test
  void invalidTokenIsNotCounted() {
    String tampered = hs256Token() + "x";

    assertThrows(RuntimeException.class, () -> service.parse(tampered));
    assertEquals(0.0, count("HS256", "accepted") + count("HS256", "rejected"));
  }

  @Test
  void ageBuckets() {
    long day = 86_400_000L;
    assertEquals("sin-iat", JwtMetrics.ageBucket(null));
    assertEquals("<1d", JwtMetrics.ageBucket(new Date()));
    assertEquals("<7d", JwtMetrics.ageBucket(new Date(System.currentTimeMillis() - 2 * day)));
    assertEquals("<30d", JwtMetrics.ageBucket(new Date(System.currentTimeMillis() - 10 * day)));
    assertEquals("<365d", JwtMetrics.ageBucket(new Date(System.currentTimeMillis() - 100 * day)));
    assertEquals(">=365d", JwtMetrics.ageBucket(new Date(System.currentTimeMillis() - 400 * day)));
    assertEquals("otro", JwtMetrics.normalizeType(null));
  }
}
