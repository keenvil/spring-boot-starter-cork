package com.keenvil.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

/**
 * Annotation to define a validation on String values with must belong to the
 * list of values defined in an Enum Class.
 */
@Documented
@Constraint(validatedBy = EnumValidatorImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ReportAsSingleViolation
public @interface EnumValidator {

  /**
   * The Enum class to match values.
   * 
   * @return enum class.
   */
  Class<? extends Enum<?>> source();

  /**
   * Default error message.
   * @return error message.
   */
  String message() default "There is not valid value in defined Enum class.";

  /**
   * Class groups.
   * @return class groups.
   */
  Class<?>[] groups() default {};

  /**
   * Class payload.
   * @return class payload.
   */
  Class<? extends Payload>[] payload() default {};
}