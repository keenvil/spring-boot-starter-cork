package com.keenvil.cork.security.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Identify a user and his roles within the application services.
 */
public class JwtUser implements UserDetails {

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

