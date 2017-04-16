package com.keenvil.cork.multitenancy;

/**
 * Encapsulates Community resolution behavior. 
 */
public abstract class CummunityResolver {

  /**
   * Returns current request Community Id.
   * 
   * @return current request Community Id
   */
  public abstract String resolve();

  /**
   * Returns default Community Id.
   * 
   * @return default Community id.
   */
  public abstract String defaultTenant();
}
