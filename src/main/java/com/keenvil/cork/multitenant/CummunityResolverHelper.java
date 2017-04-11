package com.keenvil.cork.multitenant;

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
