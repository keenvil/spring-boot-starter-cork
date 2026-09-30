package com.keenvil.cork.jwt;

import java.util.Date;

import io.micrometer.core.instrument.Metrics;

/**
 * Metrica de tokens JWT validados, para medir cuantos HS256 legacy siguen vivos antes de
 * rechazarlos (SPRINT_BACKLOG.md S10-SEC-05 / P0-SEC-04, PLAN_JWT_SIN_HS256.md).
 *
 * <p>Clase separada de {@link JwtService} a proposito: Micrometer es dependencia opcional de cork,
 * y solo se carga si esta en el classpath del servicio (ver {@link JwtService#recordToken}). Usa el
 * registro global, al que Spring Boot enlaza el de Prometheus por defecto
 * ({@code management.metrics.use-global-registry=true}).
 *
 * <p>Serie: {@code cork_jwt_tokens_total{alg, type, age, result}} — alg HS256|RS256; type
 * access|refresh|otro; age &lt;1d|&lt;7d|&lt;30d|&lt;365d|&gt;=365d|sin-iat (antiguedad desde {@code iat});
 * result accepted|rejected. Sin datos del usuario (ni subject ni username).
 */
final class JwtMetrics {

  private JwtMetrics() {
  }

  static void record(String alg, String type, Date issuedAt, String result) {
    Metrics.counter("cork.jwt.tokens", "alg", alg, "type", normalizeType(type),
        "age", ageBucket(issuedAt), "result", result).increment();
  }

  static String normalizeType(String type) {
    if ("access".equals(type) || "refresh".equals(type)) {
      return type;
    }
    return "otro";
  }

  static String ageBucket(Date issuedAt) {
    if (issuedAt == null) {
      return "sin-iat";
    }
    long hours = (System.currentTimeMillis() - issuedAt.getTime()) / 3_600_000L;
    if (hours < 24) {
      return "<1d";
    }
    if (hours < 24 * 7) {
      return "<7d";
    }
    if (hours < 24 * 30) {
      return "<30d";
    }
    if (hours < 24 * 365) {
      return "<365d";
    }
    return ">=365d";
  }
}
