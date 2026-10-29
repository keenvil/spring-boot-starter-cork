package com.keenvil.cork.openapi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Error propio de una operacion para el documento OpenAPI (5.1.0): agrega la respuesta {@code status} con el
 * esquema de error de cork y un ejemplo con {@code code}. Varios del mismo status se juntan en una respuesta.
 * Se usa en lugar de {@code @ApiResponse} de swagger para errores: con {@code @ApiResponse} springdoc deja de
 * generar el 200 y le pone a la respuesta de error el tipo que devuelve el metodo.
 *
 * <pre>
 * &#64;ApiError(status = 409, code = "amount.changed", description = "El importe cambio desde que se leyo.")
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ApiErrors.class)
public @interface ApiError {

  /** Codigo HTTP. */
  int status();

  /** {@code code} del {@code KeenvilApiError}. */
  String code();

  /** Cuando se produce, en una linea. */
  String description();
}
