package com.keenvil.cork.multitenancy;

/**
 * Multi Teanancy Specification to set up dynamically defined configuration
 * in {@link EnableMultitenancy} annotation.
 */
public class MultitenancySpecification {

  private String[] basePackage;

  /**
   * Creates a new Specification with base packages to be scanned.
   * 
   * @param theBasePackage base packages.
   */
  public MultitenancySpecification(final String... theBasePackage) {
    basePackage = theBasePackage;
  }

  public String[] getBasePackages() {
    return basePackage;
  }
}
