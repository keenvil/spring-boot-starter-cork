package com.keenvil.cork.error;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.keenvil.cork.error.KeenvilApiError.KeenvilApiErrorBuilder;
import com.keenvil.cork.error.KeenvilApiException.ResourceNotFound;
import com.keenvil.cork.error.KeenvilBusinessException.ValidationError;

/**
 * Generic Keenvil Controller Advice for Application Module APIs.
 * 
 * <p>Keenvil Modules must extend this Controller Advice in order to standardize
 * raised exceptions by Application APIs.</p>
 */
@ControllerAdvice
public class KeenvilApiControllerAdvice {

  private static Logger log = getLogger(KeenvilApiControllerAdvice.class);

  @Value("${spring.application.name}")
  private String name;

  @ExceptionHandler(ValidationError.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleValidationErrors(
      final HttpServletRequest request,
      final ValidationError exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    List<ObjectError> validationErrors = exception.getErrors();
    for (ObjectError error : validationErrors) {
      KeenvilApiError apiError = new KeenvilApiErrorBuilder()
          .code(error.getCodes()[0])
          .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY.value())
          .title("Validation Errors")
          .detail(error.getDefaultMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();

      log.error("Platform Error: {}", apiError.toString());
      errors.add(apiError);
    }
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
  }

  @ExceptionHandler(ResourceNotFound.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleEntityNotFound(
      final HttpServletRequest request,
      final ResourceNotFound exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiErrorBuilder()
          .code("entityNotFound")
          .httpStatus(HttpStatus.NOT_FOUND.value())
          .title("Entity Not Found")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();

    log.error("Platform Error: {}", error.toString());
    errors.add(error);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
  }
}
