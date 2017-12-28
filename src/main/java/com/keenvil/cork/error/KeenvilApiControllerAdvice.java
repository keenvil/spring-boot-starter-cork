package com.keenvil.cork.error;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.keenvil.cork.error.KeenvilApiError.KeenvilApiErrorBuilder;
import com.keenvil.cork.error.KeenvilApiException.Authorization;
import com.keenvil.cork.error.KeenvilApiException.BadRequest;
import com.keenvil.cork.error.KeenvilApiException.Forbidden;
import com.keenvil.cork.error.KeenvilApiException.InvalidArgument;
import com.keenvil.cork.error.KeenvilApiException.InvalidResourceState;
import com.keenvil.cork.error.KeenvilApiException.ResourceAlreadyExists;
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

  /**
   * Gets the application name.
   * 
   * @return The application name.
   */
  protected String getName() {
    return name;
  }

  @ExceptionHandler(Authorization.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleAuthorization(
      final HttpServletRequest request,
      final Authorization exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
          .code("unauthorized")
          .httpStatus(HttpStatus.UNAUTHORIZED.value())
          .title("Unauthorized")
          .detail(exception.getMessage())
          .module(getName())
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(errors);
  }

  @ExceptionHandler({ResourceNotFound.class, JpaObjectRetrievalFailureException.class})
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleResourceNotFound(
      final HttpServletRequest request,
      final Exception exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiErrorBuilder()
          .code("resourceNotFound")
          .httpStatus(HttpStatus.NOT_FOUND.value())
          .title("Resource Not Found")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(errors);
  }

  @ExceptionHandler(ResourceAlreadyExists.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleResourceAlreadyExist(
      final HttpServletRequest request,
      final ResourceAlreadyExists exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
          .code("resourceAlreadyExists")
          .httpStatus(HttpStatus.CONFLICT.value())
          .title("Resource Already Exists")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(errors);
  }

  @ExceptionHandler(InvalidResourceState.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleInvalidState(
      final HttpServletRequest request,
      final InvalidResourceState exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
          .code("invalidResourceState")
          .httpStatus(HttpStatus.PRECONDITION_FAILED.value())
          .title("Invalid Resource State")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(errors);
  }

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
      errors.add(apiError);
      log.error("Platform Error: {}", apiError.toString());
    }
    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(errors);
  }

  @ExceptionHandler(InvalidArgument.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleInvalidArgument(
      final HttpServletRequest request,
      final InvalidArgument exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
          .code("invalidArgument")
          .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY.value())
          .title("Invalid Argument")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
  }

  @ExceptionHandler(Forbidden.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleForbidden(
      final HttpServletRequest request,
      final ResourceNotFound exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiErrorBuilder()
        .code("forbidden")
        .httpStatus(HttpStatus.FORBIDDEN.value())
        .title("Forbidden")
        .detail(exception.getMessage())
        .module(name)
        .request(request)
        .source(exception)
        .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(errors);
  }

  @ExceptionHandler(BadRequest.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleBadRequest(
      final HttpServletRequest request,
      final BadRequest exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiErrorBuilder()
        .code("badRequest")
        .httpStatus(HttpStatus.BAD_REQUEST.value())
        .title("Bad Request")
        .detail(exception.getMessage())
        .module(name)
        .request(request)
        .source(exception)
        .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(errors);
  }

  @ExceptionHandler(KeenvilApiException.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleApiException(
      final HttpServletRequest request,
      final KeenvilApiException exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
          .code("internalServerError")
          .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .title("Internal Server Error")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    log.error("Platform Error: {}", error.toString());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
  }
}
