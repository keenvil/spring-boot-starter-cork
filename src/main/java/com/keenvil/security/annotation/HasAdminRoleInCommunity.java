package com.keenvil.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/** Annotation renaming for being an ADMIN user in any of user's communities.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@securityService.hasRoleInCommunity(principal, 'ADMIN')")
public @interface HasAdminRoleInCommunity { }
