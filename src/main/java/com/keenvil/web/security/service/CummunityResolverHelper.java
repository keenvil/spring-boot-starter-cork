package com.keenvil.web.security.service;

/**
 * Indented to encapsulates Community resolution behavior. 
 */
public interface CummunityResolverHelper {

  /**
   * Returns current request Community Id.
   * 
   * @return current request Community Id
   */
  String resolve();
}
