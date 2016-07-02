package com.keenvil.security.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.config.annotation.method.configuration
    .EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration
    .EnableWebSecurity;

/** Annotation renaming for Web Security enabling.
 */
@Retention(RetentionPolicy.RUNTIME)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public @interface EnableJwtSecurity {

}
