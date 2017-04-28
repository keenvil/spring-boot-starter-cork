package com.keenvil.cork;

/**
 * Encapsulates Community resolution behavior. 
 */
public abstract class CommunityIdentifierResolver {

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
