package com.keenvil.web.security.jwt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.keenvil.web.security.jwt.JwtService;
import com.keenvil.web.security.jwt.JwtService.JwtUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureAlgorithm;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JwtServiceTest {

  private JwtService service = new JwtService();

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void generate() {
    Date expirationDate = new Date((new Date()).getTime() + 3600000);
    Set<String> roles = new HashSet<String>();
    Collections.addAll(roles, "USER", "ADMIN");

    String jwt = service.generate("1", "admin@keenvil.com", roles,
        expirationDate);
    assertThat(jwt, notNullValue());

    Jwt<JwsHeader, Claims> parsed =
        Jwts.parser()
          .setSigningKey(JwtService.KEY)
          .parseClaimsJws(jwt);

    Claims claims = parsed.getBody();

    assertThat(claims.getIssuer(), is(JwtService.ISSUER));
    assertThat(claims.getIssuedAt(), notNullValue());
    assertThat(claims.getExpiration(), notNullValue());
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    assertThat(claims.getSubject(), is("1"));
    Collection<String> claimRoles = (List<String>)claims.get("roles");
    assertTrue(roles.containsAll(claimRoles) && claimRoles.containsAll(roles));
  }

  @Test
  public void parse_usernameNotPresent() {
    String jwt = Jwts.builder()
        .setSubject("me")
        .setIssuer(JwtService.ISSUER)
        .setIssuedAt(new Date())
        .signWith(SignatureAlgorithm.HS256, JwtService.KEY)
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception, is(instanceOf(MissingClaimException.class)));
    }
  }

  @Test
  public void parse_issuerNotPresent() {
    String jwt = Jwts.builder()
        .setIssuedAt(new Date())
        .signWith(SignatureAlgorithm.HS256, JwtService.KEY)
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception.getCause(),
          is(instanceOf(MissingClaimException.class)));
    }
  }

  @Test
  public void parse_issuedByunrecognizedEntity() {
    String jwt = Jwts.builder()
        .setIssuer("unrecognized")
        .signWith(SignatureAlgorithm.HS256, JwtService.KEY)
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception.getCause(),
          is(instanceOf(IncorrectClaimException.class)));
    }
  }

  @Test
  public void parse_expiredToke() {
    String jwt = Jwts.builder()
        .setIssuer(JwtService.ISSUER)
        .setExpiration(new Date(1))
        .signWith(SignatureAlgorithm.HS256, JwtService.KEY)
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception.getCause(),
          is(instanceOf(ExpiredJwtException.class)));
    }
  }

  @Test
  public void parse_noSubject() {
    String jwt = Jwts.builder()
        .setIssuer(JwtService.ISSUER)
        .signWith(SignatureAlgorithm.HS256, JwtService.KEY)
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception, is(instanceOf(MissingClaimException.class)));
    }
  }

  @Test
  public void parse_nullRoles() {
    String jwt = Jwts.builder()
        .setIssuer(JwtService.ISSUER)
        .setSubject("1")
        .signWith(SignatureAlgorithm.HS256, JwtService.KEY)
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception, is(instanceOf(MissingClaimException.class)));
    }
  }

  @Test
  public void parse() {
    Set<String> roles = new HashSet<String>();
    Collections.addAll(roles, "USER", "ADMIN");
    String jwt = service.generate("1", "admin@keenvil.com", roles);
    JwtUser userClaim = service.parse(jwt);
    assertThat(userClaim, notNullValue());
    assertThat(userClaim.getUserAccountId(), is(1L));
    Collection<? extends GrantedAuthority> authorities =
        userClaim.getAuthorities();
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void refresh() {
    Date plus10Minutes = new DateTime().plusMinutes(10).toDate();
    String jwt = service.generate("1", "admin@keenvil.com",
        Collections.<String>emptySet(), plus10Minutes);
    String refreshedJwt = service.refresh(jwt);
    assertThat(refreshedJwt, notNullValue());

    Jwt<JwsHeader, Claims> old = Jwts.parser()
        .setSigningKey(JwtService.KEY)
        .parseClaimsJws(jwt);
    Jwt<JwsHeader, Claims> refreshed = Jwts.parser()
        .setSigningKey(JwtService.KEY)
        .parseClaimsJws(refreshedJwt);

    Claims oldBody = old.getBody();
    Claims refreshedBody = refreshed.getBody();
    assertTrue(oldBody.getExpiration()
        .before(refreshedBody.getExpiration()));
  }
}
