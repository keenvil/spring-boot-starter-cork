package com.keenvil.cork.jwt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to inject current logged User in a REST service call.
 * 
 * <p>Refer to {@link LoggedUserMethodArgumentResolver} for further
 * information</p>.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoggedIndividual {}