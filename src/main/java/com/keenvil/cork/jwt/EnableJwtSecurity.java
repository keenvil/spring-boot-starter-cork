package com.keenvil.cork.jwt;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation renaming for Web Security enabling.
 *
 * <p>Spring Security 7 (Boot 4) saco EnableGlobalMethodSecurity; EnableMethodSecurity trae prePostEnabled
 * prendido. El AutoProxyRegistrar que importa solo mira las anotaciones directas de la clase que la usa, por
 * eso esta anotacion repite {@code mode} y {@code proxyTargetClass}: sin ellos @PreAuthorize no se aplica si
 * no hay otro auto-proxy (EnableJwtSecurityTest).
 */
@Retention(RetentionPolicy.RUNTIME)
@EnableWebSecurity
@EnableMethodSecurity
public @interface EnableJwtSecurity {

  AdviceMode mode() default AdviceMode.PROXY;

  boolean proxyTargetClass() default false;
}
