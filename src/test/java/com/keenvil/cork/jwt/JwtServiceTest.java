package com.keenvil.cork.jwt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.keenvil.cork.date.DateUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.Keys;

public class JwtServiceTest {

  private JwtService service = new JwtService();

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void generate() {
    Date expirationDate = new Date((new Date()).getTime() + 3600000);
    Set<String> roles = new HashSet<>();
    Collections.addAll(roles, "USER", "ADMIN");

    String jwt = service.generate("1", "Joe", "Average", "admin@keenvil.com",
        "B-52", roles,
        expirationDate);
    assertThat(jwt, notNullValue());

    Jws<Claims> parsed = Jwts.parser()
          .verifyWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
          .build()
          .parseSignedClaims(jwt);

    Claims claims = parsed.getPayload();

    assertThat(claims.getIssuer(), is(JwtService.ISSUER));
    assertThat(claims.getIssuedAt(), notNullValue());
    assertThat(claims.getExpiration(), notNullValue());
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    assertThat(claims.getSubject(), is("1"));
    Collection<String> claimRoles = (List<String>)claims.get("roles");
    assertTrue(roles.containsAll(claimRoles) && claimRoles.containsAll(roles));


    jwt = service.generate("1", "Joe", "Average", "admin@keenvil.com",
        "B-52", roles, expirationDate, "avatarUri");
    parsed = Jwts.parser()
          .verifyWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
          .build()
          .parseSignedClaims(jwt);

    claims = parsed.getPayload();
    assertThat(claims.get("avatarUri"), notNullValue());
  }

  @Test
  public void generatePusherToken() {
    Date expirationDate = new Date((new Date()).getTime() + 3600000);

    String jwt = service.generatePusherToken(expirationDate,
      "sajdlksajd", "ewewpjfpsofpdsf", "ewewpjfpsofpdsf34234324234567890", "1");

    assertThat(jwt, notNullValue());
  }

  @Test
  public void parseUsernameNotPresent() {
    String jwt = Jwts.builder()
        .subject("me")
        .issuer(JwtService.ISSUER)
        .issuedAt(new Date())
        .signWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception,
          is(instanceOf(JwtInvalidTokenException.class)));
    }
  }

  @Test
  public void parseIssuerNotPresent() {
    String jwt = Jwts.builder()
        .issuedAt(new Date())
        .signWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
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
  public void parseIssuedByunrecognizedEntity() {
    String jwt = Jwts.builder()
        .issuer("unrecognized")
        .signWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
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
  public void parseExpiredToke() {
    String jwt = Jwts.builder()
        .issuer(JwtService.ISSUER)
        .expiration(new Date(1))
        .signWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
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
  public void parseNoSubject() {
    String jwt = Jwts.builder()
        .issuer(JwtService.ISSUER)
        .signWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception,
          is(instanceOf(JwtInvalidTokenException.class)));
    }
  }

  @Test
  public void parseNullRoles() {
    String jwt = Jwts.builder()
        .issuer(JwtService.ISSUER)
        .subject("1")
        .signWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
        .compact();
    try {
      service.parse(jwt);
      fail();
    } catch (Exception exception) {
      assertThat(exception,
          is(instanceOf(JwtInvalidTokenException.class)));
    }
  }

  @Test
  public void parseInvalidToken() throws Exception {
    try {
      service.parse("invalid");
      fail();
    } catch (Exception exception) {
      assertThat(exception,
          is(instanceOf(JwtInvalidTokenException.class)));
    }
  }

  @Test
  public void parse() {
    Set<String> roles = new HashSet<>();
    Collections.addAll(roles, "USER", "ADMIN");
    String jwt = service.generate("1", "Joe", "Average", "B-52",
        "admin@keenvil.com", roles, "avatarUri");
    JwtUser userClaim = service.parse(jwt);
    assertThat(userClaim, notNullValue());
    assertThat(userClaim.getUserAccountId(), is(1L));
    Collection<? extends GrantedAuthority> authorities =
        userClaim.getAuthorities();
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    assertThat(userClaim.getFirstName(), is("Joe"));
    assertThat(userClaim.getAvatarUri(), is("avatarUri"));
  }

  @Test
  public void parseInvalid() {
    String jwt = service.generateRefresh("1", DateUtils.nowPlusMinutesInUtc(1));
    try {
      service.parse(jwt);
      fail();
    } catch (JwtInvalidTokenException exception) {
      assertThat(exception.getMessage(), is("Invalid access token."));
    }
  }

  @Test
  public void parseRefresh() {
    Set<String> roles = new HashSet<>();
    Collections.addAll(roles, "USER", "ADMIN");
    String jwt = service.generateRefresh("1", DateUtils.nowPlusMinutesInUtc(5));

    JwtUser user = service.parseRefresh(jwt);
    assertThat(user.getUserAccountId(), is(1L));
  }

  @Test
  public void parseInvalidRefresh() {
    Set<String> roles = new HashSet<>();
    Collections.addAll(roles, "USER", "ADMIN");
    String jwt = service.generate("1", "Joe", "Average", "B-52",
        "admin@keenvil.com", roles, "avatarUri");

    try {
      service.parseRefresh(jwt);
      fail();
    } catch (JwtInvalidTokenException exception) {
      assertThat(exception.getMessage(), is("Invalid refresh token."));
    }
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void generateRefreshToken() throws Exception {
    String refreshToken = service.generateRefresh("individual-id",
        DateUtils.nowPlusMinutesInUtc(10));
    assertThat(refreshToken, notNullValue());

    Jws<Claims> parseClaimsJwt = Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(refreshToken);
    assertThat(parseClaimsJwt.getPayload().getSubject(), is("individual-id"));
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void refresh() {
    Date plus10Minutes = Date.from(Instant.now().plus(10, ChronoUnit.MINUTES));
    String jwt = service.generate("1",  "Joe", "Average", "B-52",
        "admin@keenvil.com", Collections.emptySet(), plus10Minutes);
    String refreshedJwt = service.refresh(jwt);
    assertThat(refreshedJwt, notNullValue());

    Jws<Claims> old = Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(jwt);
    Jws<Claims> refreshed = Jwts.parser()
        .verifyWith(Keys.hmacShaKeyFor(JwtService.KEY.getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(refreshedJwt);

    Claims oldBody = old.getPayload();
    Claims refreshedBody = refreshed.getPayload();
    assertTrue(oldBody.getExpiration()
        .before(refreshedBody.getExpiration()));
  }
}
