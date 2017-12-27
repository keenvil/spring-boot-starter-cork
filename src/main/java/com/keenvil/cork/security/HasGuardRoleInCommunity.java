package com.keenvil.cork.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * Annotation renaming for being a Community GUARD or SUPERVISOR.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@resourceSecurityService.hasRoleInCommunity(principal," +
    " {'GUARD', 'SUPERVISOR'})")
public @interface HasGuardRoleInCommunity { }
