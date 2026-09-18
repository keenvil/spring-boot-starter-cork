package com.keenvil.cork.jwt;

import static org.slf4j.LoggerFactory.getLogger;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keenvil.cork.date.DateUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

/**
 * Generates/Refreshes JSON Web Tokens and JWT Users which can be used to
 * interact with application services.
 */
@Service
public class JwtService {

  private static Logger log = getLogger(JwtService.class);

  public static final String X_AUTHORIZATION = "X-Authorization";

  private static final String USERNAME = "username";

  private static final String FIRST_NAME = "firstName";

  private static final String LAST_NAME = "lastName";

  private static final String UNIT = "unit";

  private static final String ROLES = "roles";

  private static final String TYPE = "type";

  private static final String TYPE_ACCESS = "access";

  private static final String TYPE_REFRESH = "refresh";

  private static final String AVATAR_URI = "avatarUri";

  /** JWT time to live in minutes. */
  @Value("${jwt.ttl:120}")
  private int minutes = 120;

  /**
   * Clave HS256 legacy, compartida entre 4 codebases independientes (ver
   * incidente 2026-09-18, SPRINT_BACKLOG.md [P0-SEC-04]). Se mantiene
   * únicamente para poder seguir validando tokens emitidos antes de la
   * migración a RS256 mientras dure la transición -- sacar junto con
   * {@link #parseClaims(String)}'s fallback a HMAC una vez que no queden
   * tokens HS256 vivos (TTL máximo del refresh token).
   *
   * <p>TODO(mario-AC-25): Externalize in Vault.</p>
   */
  static final String KEY = "&....#$[myCo-key]#$....&keenvil!";

  /** TODO(mario-AC-25): Externalize in Vault. */
  static final String ISSUER = "myCo-security-api";

  /**
   * Clave pública RSA para la migración HS256 -> RS256 (ver
   * SPRINT_BACKLOG.md [P0-SEC-04]). No es secreta -- puede vivir hardcodeada
   * en el código igual que las constantes de arriba, a diferencia de la
   * privada (que solo debe existir en el runtime de security-api, nunca en
   * este JAR compartido: ver {@link #rsaPrivateKeyPem}).
   */
  private static final String RSA_PUBLIC_KEY_PEM =
      "-----BEGIN PUBLIC KEY-----\n"
      + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvypaJCfJMH2K0yWY/sAK\n"
      + "Z+LxfeEwOg9//3RuNnGYX3YeS+k456Ya1CkqIXAUTU+1USUi3+T/S5p8Ueks59MU\n"
      + "PxtcP5ISSYxM+yGHT5B+1e+tIBrKIfdezD63mzc9s4bur0N3fmXBkFhIqy3m9KD+\n"
      + "0tz+nLLriplFRaTfpTj8pcS0GGS02J1QprVv3ByQblSkjYxThg6gwOhp0ZVJr64S\n"
      + "rzN+L0bkhkA09pmlRVoRwrTrMweWNv6SDYAb58wf60WLUOaggkwkI9n7LuRebnsu\n"
      + "KOYXJiCAGvPLDZDdgZuilzmGGCijH4e4pptLSqwz8vJWrE9nAu4M07hpMTpQ7XTZ\n"
      + "fQIDAQAB\n"
      + "-----END PUBLIC KEY-----";

  private static PublicKey rsaPublicKey = loadRsaPublicKey(RSA_PUBLIC_KEY_PEM);

  /**
   * Visible for testing: permite validar el flujo RS256 de punta a punta con
   * un par de claves descartable, sin exponer la clave privada real de
   * produccion en el codigo del test.
   */
  static void setRsaPublicKeyForTesting(PublicKey key) {
    rsaPublicKey = key;
  }

  private static PublicKey loadRsaPublicKey(String pem) {
    try {
      String base64 = pem
          .replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replaceAll("\\s", "");
      byte[] decoded = Base64.getDecoder().decode(base64);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("No se pudo cargar la clave publica RSA para JWT.", e);
    }
  }

  /**
   * Clave privada RSA, inyectada solo en el runtime de security-api (el
   * único servicio que emite tokens) via la property {@code jwt.rsa-private-key}
   * -- nunca hardcodeada en el código de este JAR compartido, a diferencia de
   * la pública de arriba, porque cork lo consumen todos los servicios y
   * cualquier valor hardcodeado acá terminaria en el classpath de todos.
   * Vacía/ausente en el resto de los servicios: siguen firmando con HS256
   * hasta que termine la migración.
   */
  @Value("${jwt.rsa-private-key:}")
  private String rsaPrivateKeyPem;

  /** Visible for testing: ver {@link #setRsaPublicKeyForTesting(PublicKey)}. */
  void setRsaPrivateKeyPemForTesting(String pem) {
    rsaPrivateKeyPem = pem;
  }

  private PrivateKey rsaPrivateKey() {
    if (rsaPrivateKeyPem == null || rsaPrivateKeyPem.isBlank()) {
      return null;
    }
    try {
      String base64 = rsaPrivateKeyPem
          .replace("-----BEGIN PRIVATE KEY-----", "")
          .replace("-----END PRIVATE KEY-----", "")
          .replaceAll("\\s", "");
      byte[] decoded = Base64.getDecoder().decode(base64);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("No se pudo cargar la clave privada RSA para JWT.", e);
    }
  }

  public static class Token {

    private String access;

    private Date tokenTtl;

    private String refresh;

    private Date refreshTokenTtl;

    Token() { }

    /**
     * Token which encapsulates Access and Refresh Jwt.
     *
     * @param theAccess Access Jwt.
     * @param theRefresh Refresh Jwt.
     */
    public Token(String theAccess,
        Date theTokenTtl,
        String theRefresh,
        Date theRefreshTokenTtl) {
      Validate.notEmpty(theAccess, "Access Jwt cannot be empty.");
      Validate.notNull(theTokenTtl, "Token ttl cannot be empty.");
      Validate.notEmpty(theRefresh, "Access Jwt cannot be empty.");
      Validate.notNull(theRefreshTokenTtl,
          "Refersh Token ttl cannot be empty.");
      Validate.notEmpty(theAccess, "Access Jwt cannot be empty.");
      access = theAccess;
      tokenTtl = theTokenTtl;
      refresh = theRefresh;
      refreshTokenTtl = theRefreshTokenTtl;
    }

    public String getAccess() {
      return access;
    }

    public Date getTokenTtl() {
      return tokenTtl;
    }

    public String getRefresh() {
      return refresh;
    }

    public Date getRefreshTokenTtl() {
      return refreshTokenTtl;
    }
  }

  /**
   * Generates a JWT with default TTL, which can be used to access application
   * services.
   * @return the JWT.
   */
  public String generate(
      final String subject,
      final String firstName,
      final String lastName,
      final String unit,
      final String username,
      final Set<String> roles) {
    Date ttl = Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
    log.info("Token Expiration TTL {}", minutes);
    return generate(subject,
        firstName,
        lastName,
        unit,
        username,
        roles,
        ttl,
        null);
  }

  /**
   * Generates a JWT with default TTL, which can be used to access application
   * services with an expiration date.
   * @return the JWT.
   */
  public String generate(
      final String subject,
      final String firstName,
      final String lastName,
      final String unit,
      final String username,
      final Set<String> roles,
      final Date expirationDate) {
    return generate(subject,
        firstName,
        lastName,
        unit,
        username,
        roles,
        expirationDate,
        null);
  }

  /**
   * Generates a JWT with default TTL, which can be used to access application
   * services with an Avatar Uri.
   * @return the JWT.
   */
  public String generate(
      final String subject,
      final String firstName,
      final String lastName,
      final String unit,
      final String username,
      final Set<String> roles,
      final String avatarUri) {
    Date ttl = Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
    log.info("Token Expiration TTL {}", minutes);
    return generate(subject,
        firstName,
        lastName,
        unit,
        username,
        roles,
        ttl,
        avatarUri);
  }

  /**
   * Generates a JWT which can be used to access application services.
   * @param subject subject.
   * @param firstName first name.
   * @param lastName last name.
   * @param username user name.
   * @param unit unit.
   * @param roles roles.
   * @param expirationDate JWT expiration date.
   * @param avatarUri Avatar Uri.
   * @return the JWT.
   */
  public String generate(
      final String subject,
      final String firstName,
      final String lastName,
      final String unit,
      final String username,
      final Set<String> roles,
      final Date expirationDate,
      final String avatarUri) {
    log.trace("Entering generate.");

    Validate.notEmpty(subject);
    Validate.notEmpty(username);
    Validate.notNull(expirationDate);

    Date today = new Date();
    PrivateKey rsaKey = rsaPrivateKey();
    String jwt;
    if (rsaKey != null) {
      jwt = Jwts.builder()
          .issuer(ISSUER)
          .issuedAt(today)
          .expiration(expirationDate)
          .subject(subject)
          .claim(TYPE, TYPE_ACCESS)
          .claim(FIRST_NAME, firstName)
          .claim(LAST_NAME, lastName)
          .claim(UNIT, unit)
          .claim(USERNAME, username)
          .claim(ROLES, roles)
          .claim(AVATAR_URI, avatarUri)
          .signWith(rsaKey)
          .compact();
    } else {
      jwt = Jwts.builder()
          .issuer(ISSUER)
          .issuedAt(today)
          .expiration(expirationDate)
          .subject(subject)
          .claim(TYPE, TYPE_ACCESS)
          .claim(FIRST_NAME, firstName)
          .claim(LAST_NAME, lastName)
          .claim(UNIT, unit)
          .claim(USERNAME, username)
          .claim(ROLES, roles)
          .claim(AVATAR_URI, avatarUri)
          .signWith(Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8)))
          .compact();
    }

    log.info("Token Expiration {}", expirationDate);
    log.trace("Leaving generate.");
    return jwt;
  }

  /**
   * Generates a JWT which can be used to access application services.
   * @return the JWT.
   */
  public String generatePusherToken(
    final Date expirationDate,
    final String pusherIssuer,
    final String pusherKey,
    final String pusherSecretKey,
    final String accountId) {
    log.trace("Entering generate.");

    Date today = new Date();
    String jwt = Jwts.builder()
                   .subject(accountId)
                   .issuer(pusherKey)
                   .issuedAt(today)
                   .expiration(expirationDate)
                   .claim("instance", pusherIssuer)
                   .signWith(Keys.hmacShaKeyFor(
                       pusherSecretKey.getBytes(StandardCharsets.UTF_8)))
                   .compact();

    log.info("PusherToken Expiration [{}]", expirationDate);
    log.trace("Leaving generate.");
    return jwt;
  }

  /**
   * Generates the Refresh Token.
   *
   * @param subject Subject.
   * @return Token.
   */
  public String generateRefresh(
      String subject,
      Date ttl) {
    PrivateKey rsaKey = rsaPrivateKey();
    if (rsaKey != null) {
      return Jwts.builder()
          .issuer(ISSUER)
          .issuedAt(DateUtils.nowInUtc())
          .subject(subject)
          .expiration(ttl)
          .claim(TYPE, TYPE_REFRESH)
          .signWith(rsaKey)
          .compact();
    }
    return Jwts.builder()
        .issuer(ISSUER)
        .issuedAt(DateUtils.nowInUtc())
        .subject(subject)
        .expiration(ttl)
        .claim(TYPE, TYPE_REFRESH)
        .signWith(Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  /**
   * Generates a complete Token (access token and refresh token).
   *
   * @param subject subject.
   * @param firstName first name.
   * @param lastName last name.
   * @param username user name.
   * @param unit unit.
   * @param roles roles.
   * @param tokenExpirationDate JWT expiration date.
   * @param refreshExpirationDate JWT refresh expiration date.
   * @param avatarUri Avatar Uri.
   * @return the JWT.
   */
  public Token generateToken(
      final String subject,
      final String firstName,
      final String lastName,
      final String unit,
      final String username,
      final Set<String> roles,
      final Date tokenExpirationDate,
      final Date refreshExpirationDate,
      final String avatarUri) {
    String access = generate(subject,
        firstName,
        lastName,
        unit,
        username,
        roles,
        tokenExpirationDate,
        avatarUri);
    String refresh = generateRefresh(subject, refreshExpirationDate);
    return new Token(access,
        tokenExpirationDate,
        refresh,
        refreshExpirationDate);
  }

  /**
   * Parse a JWT and returns a JWT User to interact with the application
   *  services.
   * @param jwt the JWT to be parsed.
   * @return the JWT User.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public JwtUser parse(String jwt) {
    log.trace("Entering parse.");

    Validate.notNull(jwt);
    Jws<Claims> parsed = parseClaims(jwt);
    Claims claims = parsed.getPayload();

    String tokenType = (String) claims.get(TYPE);
    if (tokenType == null || !tokenType.equals(TYPE_ACCESS)) {
      log.error("Subject not present.");
      throw new JwtInvalidTokenException("Invalid access token.");
    }

    if (claims.getSubject() == null) {
      log.error("Subject not present.");
      throw new JwtInvalidTokenException("Subject not present.");
    }

    if (claims.get(FIRST_NAME) == null) {
      log.error("First name not present.");
      throw new JwtInvalidTokenException("First name not present.");
    }

    if (claims.get(LAST_NAME) == null) {
      log.error("Last name not present.");
      throw new JwtInvalidTokenException("Last name not present.");
    }

    if (claims.get(UNIT) == null) {
      log.error("Unit not present.");
      throw new JwtInvalidTokenException("Unit not present.");
    }

    if (claims.get(USERNAME) == null) {
      log.error("Username not present.");
      throw new JwtInvalidTokenException("Username not present.");
    }

    if (claims.get(ROLES) == null) {
      log.error("Roles not defined.");
      throw new JwtInvalidTokenException("Roles not defined.");
    }

    JwtUser jwtUser =
        new JwtUser(Long.valueOf(claims.getSubject()),
            (String) claims.get(FIRST_NAME),
            (String) claims.get(LAST_NAME),
            (String) claims.get(UNIT),
            (String) claims.get(USERNAME),
            (Set<String>) new HashSet((List<String>) claims.get(ROLES)),
            (String) claims.get(AVATAR_URI));

    log.trace("Leaving parse.");
    return jwtUser;
  }

  /**
   * Refreshes a valid JWT.
   * Generates a new JWT with an updated TTL.
   * @param jwt valid JWT to be regenerated
   * @return new JWT
   */
  public String refresh(final String jwt) {
    log.trace("Entering refresh.");
    Validate.notEmpty(jwt);

    JwtUser jwtUser = parse(jwt);

    Date today = new Date();
    Date plusMinutes = Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
    PrivateKey rsaKey = rsaPrivateKey();
    String refreshed;
    if (rsaKey != null) {
      refreshed = Jwts.builder()
          .issuer(ISSUER)
          .issuedAt(today)
          .expiration(plusMinutes)
          .subject(jwtUser.getUserAccountId().toString())
          .claim(FIRST_NAME, jwtUser.getFirstName())
          .claim(LAST_NAME, jwtUser.getLastName())
          .claim(UNIT, jwtUser.getUnit())
          .claim(USERNAME, jwtUser.getUsername())
          .claim(ROLES, jwtUser.getRoles())
          .signWith(rsaKey)
          .compact();
    } else {
      refreshed = Jwts.builder()
          .issuer(ISSUER)
          .issuedAt(today)
          .expiration(plusMinutes)
          .subject(jwtUser.getUserAccountId().toString())
          .claim(FIRST_NAME, jwtUser.getFirstName())
          .claim(LAST_NAME, jwtUser.getLastName())
          .claim(UNIT, jwtUser.getUnit())
          .claim(USERNAME, jwtUser.getUsername())
          .claim(ROLES, jwtUser.getRoles())
          .signWith(Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8)))
          .compact();
    }

    log.trace("Leaving refresh.");
    return refreshed;
  }

  /**
   * Parse a refresh JWT.
   *
   * @param refreshJwt Refresh JWT.
   * @return JWT User.
   */
  public JwtUser parseRefresh(final String refreshJwt) {
    Jws<Claims> parsedClaims = parseClaims(refreshJwt);
    String type = (String) parsedClaims.getPayload().get(TYPE);
    Date expiration = parsedClaims.getPayload().getExpiration();

    if (type == null || !type.equals(TYPE_REFRESH)) {
      log.error("Invalid refresh token. Type must be Refresh, but is [{}]", type);
      throw new JwtInvalidTokenException("Invalid refresh token.");
    }

    Long id = Long.valueOf(parsedClaims.getPayload().getSubject());
    return new JwtUser(id, expiration);
  }

  private Jws<Claims> parseClaims(String jwt) {
    // Migracion HS256 -> RS256 (SPRINT_BACKLOG.md [P0-SEC-04]): probar RS256
    // primero; solo si falla especificamente por firma (no es un token RS256,
    // probablemente legacy) reintentar con la clave HMAC compartida. Sacar el
    // catch de abajo y este comentario una vez que no queden tokens HS256
    // vivos (TTL maximo del refresh token) y dejar unicamente el intento RSA.
    try {
      return parseClaimsWithKey(jwt, rsaPublicKey);
    } catch (JwtInvalidTokenException rsaFailure) {
      // jjwt tira SignatureException si el token es RS256 pero con otra clave,
      // y UnsupportedJwtException si directamente el algoritmo del token (ej.
      // HS256 legacy) no es compatible con una clave RSA -- ambos casos
      // significan "no es un token RS256 valido", no "token invalido de
      // verdad", asi que ambos disparan el reintento con HMAC.
      Throwable cause = rsaFailure.getCause();
      if (!(cause instanceof SignatureException) && !(cause instanceof UnsupportedJwtException)) {
        throw rsaFailure;
      }
      log.info("Token no valido con clave RSA, reintentando con HMAC (legacy).");
      return parseClaimsWithKey(jwt, Keys.hmacShaKeyFor(KEY.getBytes(StandardCharsets.UTF_8)));
    }
  }

  private Jws<Claims> parseClaimsWithKey(String jwt, PublicKey key) {
    try {
      return Jwts.parser()
          .requireIssuer(ISSUER)
          .verifyWith(key)
          .build()
          .parseSignedClaims(jwt);
    } catch (IllegalArgumentException e) {
      log.error("Illegal Argument Exception.");
      throw new JwtInvalidTokenException("Invalid Token, Illegal Argument .",
          e);
    } catch (MissingClaimException mce) {
      log.error("Issuer not present.");
      throw new JwtInvalidTokenException("Invalid Token, issuer not present.",
          mce);
    } catch (IncorrectClaimException ice) {
      log.error("Unrecognized issuer.");
      throw new JwtInvalidTokenException("Invalid Token, unrecognized issuer.",
          ice);
    } catch (ExpiredJwtException ee) {
      log.error("Expired jwt.");
      throw new JwtExpiredTokenException("Token expired.", ee);
    } catch (SignatureException | UnsupportedJwtException rsaMismatch) {
      // Este overload (PublicKey) SOLO se usa para el intento RSA optimista de
      // parseClaims -- durante la migracion HS256->RS256 esto va a fallar para
      // practicamente el 100% de los tokens vivos (todavia HS256), es el caso
      // ESPERADO que dispara el fallback a HMAC, no un error real. Loguear esto
      // a nivel WARN/ERROR con stack trace inundaba los logs de produccion
      // (confirmado en vivo: cientos de traces por minuto en toda la flota).
      // Si el intento de fallback con HMAC tambien falla, ESE si se loguea
      // normal mas abajo (overload de SecretKey) porque ahi ya es un rechazo
      // final de verdad.
      log.debug("Token no firmado con RSA (probablemente HS256 legacy).", rsaMismatch);
      throw new JwtInvalidTokenException("Invalid Token, signature mismatch.",
          rsaMismatch);
    } catch (Exception exception) {
      log.error("Error parsing JWT. ", exception);
      throw new JwtInvalidTokenException("Error parsing Token.", exception);
    }
  }

  private Jws<Claims> parseClaimsWithKey(String jwt, SecretKey key) {
    try {
      return Jwts.parser()
          .requireIssuer(ISSUER)
          .verifyWith(key)
          .build()
          .parseSignedClaims(jwt);
    } catch (IllegalArgumentException e) {
      log.error("Illegal Argument Exception.");
      throw new JwtInvalidTokenException("Invalid Token, Illegal Argument .",
          e);
    } catch (MissingClaimException mce) {
      log.error("Issuer not present.");
      throw new JwtInvalidTokenException("Invalid Token, issuer not present.",
          mce);
    } catch (IncorrectClaimException ice) {
      log.error("Unrecognized issuer.");
      throw new JwtInvalidTokenException("Invalid Token, unrecognized issuer.",
          ice);
    } catch (ExpiredJwtException ee) {
      log.error("Expired jwt.");
      throw new JwtExpiredTokenException("Token expired.", ee);
    } catch (SignatureException se) {
      log.warn("JWT signature does not match. Token rejected.");
      throw new JwtInvalidTokenException("Invalid Token, signature mismatch.",
          se);
    } catch (Exception exception) {
      log.error("Error parsing JWT. ", exception);
      throw new JwtInvalidTokenException("Error parsing Token.", exception);
    }
  }
}
