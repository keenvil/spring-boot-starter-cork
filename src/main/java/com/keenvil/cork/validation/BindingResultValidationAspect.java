package com.keenvil.cork.validation;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import jakarta.validation.Valid;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import com.keenvil.cork.error.KeenvilApiControllerAdvice;
import com.keenvil.cork.error.KeenvilBusinessException;
import com.keenvil.cork.error.KeenvilBusinessException.ValidationError;

/**
 * Aspect which checks for annotated method with {@link ValidateRequestBody} to
 * validate if some business validation error has occurred.
 * 
 * <p>Keenvil Controllers use {@link Valid} annotation and {@link BindingResult}
 * objects to validate API parameters. Using {@link ValidateRequestBody}</p>
 * this aspect will check if some validation has failed and it will throw
 * {@link KeenvilBusinessException.ValidationError} which will be handled by
 * {@link KeenvilApiControllerAdvice} and converted to a
 * {@link HttpStatus#UNPROCESSABLE_ENTITY}.
 */
@Aspect
@Component
public class BindingResultValidationAspect {

  private static Logger log = getLogger(BindingResultValidationAspect.class);

  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  public @interface ValidateRequestBody { }

  /**
   * Checks if a method annotated with {@link ValidateRequestBody} has any
   * validation error.
   * 
   * @param joinPoint Join Point.
   * @throws ValidationError if some validation has failed.
   */
  @Before(value = "@annotation("
      + "com.keenvil.cork.validation.BindingResultValidationAspect.ValidateRequestBody)")
  public void validate(JoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    BindingResult binding =
        (BindingResult) Stream.of(args)
        .filter(o -> o instanceof BindingResult)
        .findFirst().orElse(null);

    if (log.isInfoEnabled()) {
      log.info("Validating request body for method {}.",
          joinPoint.getSignature().getName());
    }

    if (binding != null && binding.hasErrors()) {
      throw new ValidationError(binding.getAllErrors());
    }
  }
}
