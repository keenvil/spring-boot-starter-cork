package com.keenvil.cork.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation renaming for being an RESIDENT user in any of user's communities.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@resourceSecurityService.hasRoleInCommunity(principal, 'RESIDENT')")
public @interface HasResidentRoleInCommunity { }
