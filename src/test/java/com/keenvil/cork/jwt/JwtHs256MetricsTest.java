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

  // El registro global es compartido entre tests de la misma JVM: se mide por diferencia y por tags.
  private double count(String alg, String result) {
    return registry.find("cork.jwt.tokens").tag("alg", alg).tag("result", result).counters()
        .stream().mapToDouble(c -> c.count()).sum();
  }

  private double count(String alg, String type, String age, String result) {
    return registry.find("cork.jwt.tokens").tag("alg", alg).tag("type", type).tag("age", age)
        .tag("result", result).counters().stream().mapToDouble(c -> c.count()).sum();
  }

  @Test
  void hs256IsAcceptedAndCountedByDefault() {
    String jwt = hs256Token();
    double before = count("HS256", "access", "<1d", "accepted");
    double rejectedBefore = count("HS256", "rejected");

    JwtUser user = service.parse(jwt);

    assertNotNull(user);
    assertEquals(before + 1, count("HS256", "access", "<1d", "accepted"));
    assertEquals(rejectedBefore, count("HS256", "rejected"));
  }

  @Test
  void hs256IsRejectedWhenFlagIsOn() {
    service.setRejectHs256ForTesting(true);
    String jwt = hs256Token();
    double rejectedBefore = count("HS256", "rejected");
    double acceptedBefore = count("HS256", "accepted");

    assertThrows(JwtInvalidTokenException.class, () -> service.parse(jwt));
    assertEquals(rejectedBefore + 1, count("HS256", "rejected"));
    assertEquals(acceptedBefore, count("HS256", "accepted"));
  }

  @Test
  void invalidTokenIsNotCounted() {
    String tampered = hs256Token() + "x";
    double before = count("HS256", "accepted") + count("HS256", "rejected");

    assertThrows(RuntimeException.class, () -> service.parse(tampered));
    assertEquals(before, count("HS256", "accepted") + count("HS256", "rejected"));
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
