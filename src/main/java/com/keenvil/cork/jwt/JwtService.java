package com.keenvil.cork.jwt;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keenvil.cork.date.DateUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Generates/Refreshes JSON Web Tokens and JWT Users which can be used to
 * interact with application services.
 * Token encrypts: TBD Define what to we want to put in here
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


  /** TODO(mario-AC-25): Externalize in Vault. */
  static final String KEY = "&....#$[myCo-key]#$....&";
  
  /** TODO(mario-AC-25): Externalize in Vault. */
  static final String ISSUER = "myCo-security-api";

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
    Date ttl = DateTime.now().plusMinutes(minutes).toDate();
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
    Date ttl = DateTime.now().plusMinutes(minutes).toDate();
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
    String jwt = Jwts.builder()
        .setIssuer(ISSUER)
        .setIssuedAt(today)
        .setExpiration(expirationDate)
        .setSubject(subject)
        .claim(TYPE, TYPE_ACCESS)
        .claim(FIRST_NAME, firstName)
        .claim(LAST_NAME, lastName)
        .claim(UNIT, unit)
        .claim(USERNAME, username)
        .claim(ROLES, roles)
        .claim(AVATAR_URI, avatarUri)
        .signWith(SignatureAlgorithm.HS256, KEY)
        .compact();

    log.info("Token Expiration {}", expirationDate);
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
    return Jwts.builder()
        .setIssuer(ISSUER)
        .setIssuedAt(DateUtils.nowInUtc())
        .setSubject(subject)
        .setExpiration(ttl)
        .claim(TYPE, TYPE_REFRESH)
        .signWith(SignatureAlgorithm.HS256, KEY)
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
    Jwt<JwsHeader, Claims> parsed = parseClaims(jwt);
    Claims claims = parsed.getBody();

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

    DateTime today = new DateTime();
    DateTime plusMinutes = today.plusMinutes(minutes);
    String refreshed = Jwts.builder()
        .setIssuer(ISSUER)
        .setIssuedAt(today.toDate())
        .setExpiration(plusMinutes.toDate())
        .setSubject(jwtUser.getUserAccountId().toString())
        .claim(FIRST_NAME, jwtUser.getFirstName())
        .claim(LAST_NAME, jwtUser.getLastName())
        .claim(UNIT, jwtUser.getUnit())
        .claim(USERNAME, jwtUser.getUsername())
        .claim(ROLES, jwtUser.getRoles())
        .signWith(SignatureAlgorithm.HS256, KEY)
        .compact();

    log.trace("Leaving refresh.");
    return refreshed;
  }

  /**
   * Parse a refresh JWT.
   * 
   * @param refreshJwt Refresh JWT.
   * @return JWT User.
   */
  @SuppressWarnings("rawtypes")
  public JwtUser parseRefresh(final String refreshJwt) {
    Jwt<JwsHeader, Claims> parseClaims = parseClaims(refreshJwt);
    String type = (String) parseClaims.getBody().get(TYPE);

    if (type == null || !type.equals(TYPE_REFRESH)) {
      throw new JwtInvalidTokenException("Invalid refresh token.");
    }

    Long id = Long.valueOf(parseClaims.getBody().getSubject());
    return new JwtUser(id);
  }

  @SuppressWarnings("rawtypes")
  private Jwt<JwsHeader, Claims> parseClaims(String jwt) {
    Jwt<JwsHeader, Claims> parsed = null;
    try {
      parsed = Jwts.parser()
          .requireIssuer(ISSUER)
          .setSigningKey(JwtService.KEY)
          .parseClaimsJws(jwt);
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
    } catch (Exception exception) {
      log.error("Error parsing JWT. ", exception);
      throw new JwtInvalidTokenException("Error parsing Token.", exception);
    }
    return parsed;
  }
}
