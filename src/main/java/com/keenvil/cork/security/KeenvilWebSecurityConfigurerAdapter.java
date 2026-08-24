package com.keenvil.cork.security;

import java.util.Collections;
import java.util.List;

public class KeenvilWebSecurityConfigurerAdapter {

  /**
   * Gets list of end points that must be excluded from authentication
   * from a application in the form of AntMatchers.
   *
   * @return The list of endpoints in the form of AntMatchers.
   */
  @SuppressWarnings("unchecked")
  public List<String> excludeFromAuthentication() {
    return Collections.EMPTY_LIST;
  }
}
