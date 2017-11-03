package com.keenvil.cork.jwt;

import java.util.*;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Identify a user and his roles within the application services.
 */
public class JwtUser implements UserDetails {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String username;

  private Set<String> roles = new HashSet<String>();

  private String firstName;

  private String lastName;

  private String unit;

  private String avatarUri;

  private Date expiration;

  JwtUser() {
  }

  public JwtUser(final Long theId) {
    Validate.notNull(theId);
    id = theId;
  }

  public JwtUser(final Long theId, final Date theExpiration) {
    Validate.notNull(theId);
    id = theId;
    expiration = theExpiration;
  }

  /**
   * Creates a JWT User.
   *
   * @param theId        the id,
   * @param theFirstName first name.
   * @param theLastName  last name.
   * @param theUnit      unit.
   * @param theUsername  user name.
   * @param setOfRoles   roles.
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

  /**
   * Creates a JWT User.
   *
   * @param theId        the id,
   * @param theFirstName first name.
   * @param theLastName  last name.
   * @param theUnit      unit.
   * @param theUsername  user name.
   * @param setOfRoles   roles.
   */
  public JwtUser(final Long theId, final String theFirstName,
                 final String theLastName, final String theUnit,
                 final String theUsername, final Set<String> setOfRoles,
                 final Date theExpiration) {
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
    expiration = theExpiration;
  }

  public JwtUser(final Long theId, final String theFirstName,
                 final String theLastName, final String theUnit,
                 final String theUsername, final Set<String> setOfRoles,
                 final String theAvatarUri) {
    this(theId, theFirstName, theLastName, theUnit, theUsername, setOfRoles);
    avatarUri = theAvatarUri;
  }

  public JwtUser(final Long theId, final String theFirstName,
                 final String theLastName, final String theUnit,
                 final String theUsername, final Set<String> setOfRoles,
                 final String theAvatarUri, final Date theExpiration) {
    this(theId, theFirstName, theLastName, theUnit, theUsername, setOfRoles, theExpiration);
    avatarUri = theAvatarUri;
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

  public String getAvatarUri() {
    return avatarUri;
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
   *
   * @param rolesUser   role to verify.
   * @param communityId community to verify.
   * @return whether the user has the given role in the given community.
   */
  public boolean hasRoleInCommunity(final List<String> rolesUser,
                                    final String communityId) {
    return roles
        .stream()
        .anyMatch(r -> rolesUser.stream()
            .anyMatch(role -> role.concat("_").concat(communityId).equals(r)));
  }

  @Override
  public String getPassword() {
    return null;
  }


  public Date getExpiration() {
    return expiration;
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

