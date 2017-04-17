package com.keenvil.cork.multitenancy;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Condition that must be match to fire {@link MultitenancyAutoConfiguration}.
 * 
 * <p>To fire {@link MultitenancyAutoConfiguration} a property named
 * {@code keenvil.cork.multitenancy.tenants-strategy} with value
 * {@code SHARED-DB-SEPARATE-SCHEMAS} must be defined.</p>
 * 
 * <p>At this moment only {@code SHARED-DB-SEPARATE-SCHEMAS} approach is
 * supported.</p>
 */
public class MultitenancyCondition extends AnyNestedCondition {

  public MultitenancyCondition() {
    super(ConfigurationPhase.PARSE_CONFIGURATION);
  }
  
  /**
   * Property and value that must be defined.
   */
  @ConditionalOnProperty(
      prefix = "keenvil.cork.multitenancy",
      name = "tenants-strategy",
      havingValue = "SHARED-DB-SEPARATE-SCHEMAS")
  static class TenantsStrategyProperty { }
}