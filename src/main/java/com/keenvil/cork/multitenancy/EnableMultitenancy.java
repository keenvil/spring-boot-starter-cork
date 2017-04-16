package com.keenvil.cork.multitenancy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.Entity;

import org.springframework.context.annotation.Import;

/**
 * Enables Multi Tenancy support.
 * 
 * <p>Multi tenancy refers to aa architecture in which a single instance
 * on a server, serves multiple tenants.</p>
 * 
 * <p>A tenant is a group of users who share a common access with specific
 * privileges to the software instance.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MultitenancyRegistrar.class)
public @interface EnableMultitenancy {

  /**
   * Base package for module domain objects.
   * 
   * <p>Multi Tenancy will scan for {@link Entity} annotated objects.</p>
   * 
   * @return Base packages array.
   */
  String[] basePackages() default { };
}
