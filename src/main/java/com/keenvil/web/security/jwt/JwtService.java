package com.keenvil.web.security.jwt;

import static org.slf4j.LoggerFactory.getLogger;

import com.keenvil.core.error.PlatformException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureAlgorithm;

import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  /** JWT time to live in minutes. */
  @Value("${jwt.ttl:120}")
  private int minutes = 120;

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

    private String firstName;

    private String lastName;

    private String unit;

    JwtUser() { }

    /**
     * Creates a JWT User.
     * @param theId the id,
     * @param theFirstName first name.
     * @param theLastName last name.
     * @param theUnit unit.
     * @param theUsername user name.
     * @param setOfRoles roles.
     */
    public JwtUser(final Long theId, final String theFirstName,
        final String theLastName, final String theUnit,
        final String theUsername, final Set<String> setOfRoles) {
      Validate.notNull(theId);
      Validate.notNull(theFirstName);
      Validate.notNull(theLastName);
      Validate.notNull(theUnit);
      Validate.notNull(theUsername);
      Validate.notNull(setOfRoles);
      id = theId;
      firstName = theFirstName;
      lastName = theLastName;
      unit = theUnit;
      username = theUsername;
      roles = setOfRoles;
    }

    public Long getUserAccountId() {
      return id;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public String getUnit() {
      return unit;
    }

    public Set<String> getRoles() {
      return Collections.unmodifiableSet(roles);
    }

    @Override
    public String getUsername() {
      return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
      for (String authority : roles) {
        auths.add(new SimpleGrantedAuthority("ROLE_" + authority));
      }
      return auths;
    }
    
    /**
     * Return {@code true} if JWT User has an given role in a community.
     * @param role role to verify.
     * @param communityId community to verify.
     * @return whether the user has the given role in the given community.
     */
    public boolean hasRoleInCommunity(final String role,
        final String communityId) {
      return roles
          .stream()
          .anyMatch(r -> r.equals(role.concat("_").concat(communityId)));
    }

    @Override
    public String getPassword() {
      return null;
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

    @Override
    public String toString() {
      return String.format("id: %s, username:%s", id, username);
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
    return generate(subject, firstName, lastName, unit, username, roles, ttl);
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
        .claim(FIRST_NAME, firstName)
        .claim(LAST_NAME, lastName)
        .claim(UNIT, unit)
        .claim(USERNAME, username)
        .claim(ROLES, roles)
        .signWith(SignatureAlgorithm.HS256, KEY)
        .compact();

    log.trace("Leaving generate.");
    return jwt;
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
    Jwt<JwsHeader, Claims> parsed = null;
    try {
      parsed = Jwts.parser()
          .requireIssuer(ISSUER)
          .setSigningKey(JwtService.KEY)
          .parseClaimsJws(jwt);
    } catch (MissingClaimException mce) {
      log.error("Issuer not present.");
      throw new PlatformException
          .InvalidJwtToken("Invalid Token, issuer not present.", mce);
    } catch (IncorrectClaimException ice) {
      log.error("Unrecognized issuer.");
      throw new PlatformException
          .InvalidJwtToken("Invalid Token, unrecognized issuer.", ice);
    } catch (ExpiredJwtException ee) {
      log.error("Expired jwt.");
      throw new PlatformException.InvalidJwtToken("Token expired.", ee);
    } catch (Exception exception) {
      log.error("Error parsing JWT. ", exception);
      throw new PlatformException.InvalidJwtToken("Error parsing Token.",
          exception);
    }

    Claims claims = parsed.getBody();

    if (claims.getSubject() == null) {
      log.error("Subject not present.");
      throw new PlatformException.InvalidJwtToken("Subject not present.");
    }

    if (claims.get(FIRST_NAME) == null) {
      log.error("First name not present.");
      throw new PlatformException.InvalidJwtToken("First name not present.");
    }

    if (claims.get(LAST_NAME) == null) {
      log.error("Last name not present.");
      throw new PlatformException.InvalidJwtToken("Last name not present.");
    }

    if (claims.get(UNIT) == null) {
      log.error("Unit not present.");
      throw new PlatformException.InvalidJwtToken("Unit not present.");
    }

    if (claims.get(USERNAME) == null) {
      log.error("Username not present.");
      throw new PlatformException.InvalidJwtToken("Username not present.");
    }

    if (claims.get(ROLES) == null) {
      log.error("Roles not defined.");
      throw new PlatformException.InvalidJwtToken("Roles not defined.");
    }

    JwtUser jwtUser =
        new JwtUser(Long.valueOf(claims.getSubject()),
            (String) claims.get(FIRST_NAME),
            (String) claims.get(LAST_NAME),
            (String) claims.get(UNIT),
            (String) claims.get(USERNAME),
            (Set<String>) new HashSet((List<String>) claims.get(ROLES)));

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
}
