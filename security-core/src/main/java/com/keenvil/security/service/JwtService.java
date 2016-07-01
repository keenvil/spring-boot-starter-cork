package com.keenvil.security.service;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureAlgorithm;

/** Generates/Refreshes JSON Web Tokens and JWT Users which can be used to
 * interact with application services.
 * 
 * Token encrypts: TBD <Define what to we want to put in here>
 */
@Service
public class JwtService {

  private static Logger log = getLogger(JwtService.class);


  public static final String X_AUTHORIZATION = "X-Authorization";

  private static final String USERNAME = "username";

  private static final String ROLES = "roles";

  /** JWT time to live in minutes. */
  @Value("${jwt.ttl}")
  private int minutes = 15;

  /** TODO(mario-AC-25): Externalize in Vault. */
  static final String KEY = "&....#$[myCo-key]#$....&";
  
  /** TODO(mario-AC-25): Externalize in Vault. */
  static final String ISSUER = "myCo-security-api";

  /** Identify a user and his roles within the application services.
   */
  public static class JwtUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private Set<String> roles = new HashSet<String>();

    JwtUser() { }

    public JwtUser(final Long anId, final String aUsername,
        final Set<String> setOfRoles) {
      Validate.notNull(anId);
      Validate.notNull(aUsername);
      Validate.notNull(setOfRoles);
      id = anId;
      username = aUsername;
      roles = setOfRoles;
    }

    public Long getUserAccountId() {
      return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
      for (String authority : roles) {
        auths.add(new SimpleGrantedAuthority("ROLE_" + authority));
      }
      return auths;
    }

    public Set<String> getRoles() {
      return Collections.unmodifiableSet(roles);
    }

    @Override
    public String getPassword() {
      return null;
    }

    @Override
    public String getUsername() {
      return username;
    }

    @Override
    public boolean isAccountNonExpired() {
      return true;
    }

    @Override
    public boolean isAccountNonLocked() {
      return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }
  }

  /** Generates a JWT with default TTL, which can be used to access application
   * services.
   * @param account to generate the JWT.
   * @return the JWT.
   */
  public String generate(
      final String subject,
      final String username,
      final Set<String> roles) {
    Date ttl = DateTime.now().plusMinutes(minutes).toDate();
    return generate(subject, username, roles, ttl);
  }

  /** Generates a JWT which can be used to access application services.
   * @param account to generate the JWT.
   * @param expirationDate JWT expiration date.
   * @return the JWT.
   */
  public String generate(
      final String subject,
      final String username,
      final Set<String> roles,
      final Date expirationDate) {
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
        .claim(USERNAME, username)
        .claim(ROLES, roles)
        .signWith(SignatureAlgorithm.HS256, KEY)
        .compact();

    log.trace("Leaving generate.");
    return jwt;
  }

  /** Parse a JWT and returns a JWT User to interact with the application
   *  services.
   * @param jwt the JWT to be parsed.
   * @return the JWT User.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public JwtUser parse(String jwt) {
    log.trace("Entering parse.");

    Validate.notNull(jwt);
    Jwt<JwsHeader, Claims> parsed = null;
    try {
        parsed = Jwts.parser()
          .requireIssuer(ISSUER)
          .setSigningKey(JwtService.KEY)
          .parseClaimsJws(jwt);
    } catch(MissingClaimException mce) {
      log.error("Issuer not present.");
      throw new RuntimeException("Invalid JWT.", mce);
    } catch(IncorrectClaimException ice) {
      log.error("Unrecognized issuer.");
      throw new RuntimeException("Invalid JWT.", ice);
    } catch (ExpiredJwtException ee) {
      log.error("Expired jwt.");
      throw new RuntimeException("Expired JWT.", ee);
    } catch (Exception e) {
      log.error("Error parsing JWT. ", e);
      throw new MalformedJwtException("Error parsing JWT.", e);
    }

    Claims claims = parsed.getBody();

    if (claims.getSubject() == null) {
      log.error("Subject not present.");
      throw new MissingClaimException(
          parsed.getHeader(), parsed.getBody(), "Subject not present.");
    }

    if (claims.get(USERNAME) == null) {
      log.error("Username not present.");
      throw new MissingClaimException(
          parsed.getHeader(), parsed.getBody(), "Username not present.");
    }

    if (claims.get(ROLES) == null) {
      log.error("Roles not defined.");
      throw new MissingClaimException(
          parsed.getHeader(), parsed.getBody(), "Roles not defined.");
    }

    JwtUser JwtUser =
        new JwtUser(Long.valueOf(claims.getSubject()),
            (String) claims.get(USERNAME),
            (Set<String>) new HashSet((List<String>)claims.get(ROLES)));

    log.trace("Leaving parse.");
    return JwtUser;
  }

  /** Refreshes a valid JWT.
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
        .claim(USERNAME, jwtUser.getUsername())
        .claim(ROLES, jwtUser.getRoles())
        .signWith(SignatureAlgorithm.HS256, KEY)
        .compact();

    log.trace("Leaving refresh.");
    return refreshed;
  }
}
