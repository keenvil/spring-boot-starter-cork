package io.mycommunity.security.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.access.prepost.PreAuthorize;

/** Annotation renaming for being an ADMIN user in any of user's communities.
 */
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@securityService.hasRoleInCommunity(principal, 'ADMIN')")
public @interface HasAdminRoleInCommunity { }
