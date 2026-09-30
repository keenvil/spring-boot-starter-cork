package com.keenvil.cork.error;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.ErrorResponse;

import com.keenvil.cork.error.KeenvilApiError.KeenvilApiErrorBuilder;
import com.keenvil.cork.error.KeenvilApiException.Authorization;
import com.keenvil.cork.error.KeenvilApiException.BadRequest;
import com.keenvil.cork.error.KeenvilApiException.Forbidden;
import com.keenvil.cork.error.KeenvilApiException.InvalidArgument;
import com.keenvil.cork.error.KeenvilApiException.InvalidResourceState;
import com.keenvil.cork.jwt.JwtInvalidTokenException;
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

  /**
   * 4xx are client errors (not found, bots probing paths, invalid input): one WARN line without
   * the stack, so they do not flood the ERROR level. 5xx keep the full ERROR line with the source.
   * The response body is not affected.
   */
  static void logError(final KeenvilApiError error) {
    if (error.getHttpStatus() >= 500) {
      log.error("Platform Error: {}", error.toString());
    } else {
      log.warn("Platform Warning: errorId: {}, httpStatus: {}, code: {}, httpMethod: {}, uri: {}",
          error.getErrorId(), error.getHttpStatus(), error.getCode(), error.getHttpMethod(),
          error.getUri());
    }
  }

  @ExceptionHandler(Authorization.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleAuthorization(
      final HttpServletRequest request,
      final Authorization exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
          .code(StringUtils.isNotEmpty(exception.getCode()) ? exception.getCode() : "unauthorized")
          .httpStatus(HttpStatus.UNAUTHORIZED.value())
          .title("Unauthorized")
          .detail(exception.getMessage())
          .module(getName())
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    logError(error);
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(errors);
  }

  @ExceptionHandler(JwtInvalidTokenException.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleJwtInvalidToken(
      final HttpServletRequest request,
      final JwtInvalidTokenException exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiErrorBuilder()
          .code("unauthorized")
          .httpStatus(HttpStatus.UNAUTHORIZED.value())
          .title("Unauthorized")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    logError(error);
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(errors);
  }

  @ExceptionHandler({ResourceNotFound.class, JpaObjectRetrievalFailureException.class})
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleResourceNotFound(
      final HttpServletRequest request,
      final ResourceNotFound exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiErrorBuilder()
          .code(StringUtils.isNotEmpty(exception.getCode()) ? exception.getCode() : "resourceNotFound")
          .httpStatus(HttpStatus.NOT_FOUND.value())
          .title("Resource Not Found")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    logError(error);
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
          .code(StringUtils.isNotEmpty(exception.getCode()) ? exception.getCode() : "resourceAlreadyExists")
          .httpStatus(HttpStatus.CONFLICT.value())
          .title("Resource Already Exists")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    logError(error);
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
          .code(StringUtils.isNotEmpty(exception.getCode()) ? exception.getCode() : "invalidResourceState")
          .httpStatus(HttpStatus.PRECONDITION_FAILED.value())
          .title("Invalid Resource State")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    logError(error);
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
      logError(apiError);
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
          .code(StringUtils.isNotEmpty(exception.getCode()) ? exception.getCode() : "invalidArgument")
          .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY.value())
          .title("Invalid Argument")
          .detail(exception.getMessage())
          .module(name)
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    logError(error);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errors);
  }

  @ExceptionHandler(Forbidden.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleForbidden(
      final HttpServletRequest request,
      final Forbidden exception) {
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiErrorBuilder()
        .code(StringUtils.isNotEmpty(exception.getCode()) ? exception.getCode() : "forbidden")
        .httpStatus(HttpStatus.FORBIDDEN.value())
        .title("Forbidden")
        .detail(exception.getMessage())
        .module(name)
        .request(request)
        .source(exception)
        .build();
    errors.add(error);

    logError(error);
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
        .code(StringUtils.isNotEmpty(exception.getCode()) ? exception.getCode() : "badRequest")
        .httpStatus(HttpStatus.BAD_REQUEST.value())
        .title("Bad Request")
        .detail(exception.getMessage())
        .module(name)
        .request(request)
        .source(exception)
        .build();
    errors.add(error);

    logError(error);
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

    logError(error);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
  }

  /**
   * Last-resort handler for any unexpected {@link RuntimeException} (NPE, data access errors, ...).
   * Before this, those escaped the advice: Spring answered its default 500 body and nothing was
   * logged, so the cause was lost. Now they are logged as ERROR with the errorId and the full
   * stack trace, and answered with the standard error list.
   *
   * <p>It handles {@link RuntimeException} and not {@link Exception} on purpose: some services
   * declare their own {@code @ExceptionHandler(Exception.class)} in the subclass, and a second one
   * for the same type would make Spring fail at startup ("Ambiguous @ExceptionHandler").</p>
   *
   * <ul>
   *   <li>Spring Security exceptions are rethrown so its filters keep answering 401/403.</li>
   *   <li>Spring MVC client errors keep their status (4xx) and are logged as WARN.</li>
   * </ul>
   */
  @ExceptionHandler(RuntimeException.class)
  @ResponseBody ResponseEntity<List<KeenvilApiError>> handleUnexpectedException(
      final HttpServletRequest request,
      final RuntimeException exception) {
    if (isSecurityException(exception)) {
      throw exception;
    }
    HttpStatusCode status = resolveStatus(exception);
    boolean serverError = status.is5xxServerError();
    List<KeenvilApiError> errors = new ArrayList<>();
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder()
          .code(serverError ? "internalError" : status.value() == 400 ? "badRequest" : "clientError")
          .httpStatus(status.value())
          .title(reasonPhrase(status))
          .detail(serverError ? "Unexpected error" : exception.getMessage())
          .module(getName())
          .request(request)
          .source(exception)
          .build();
    errors.add(error);

    if (serverError) {
      log.error("Unhandled exception, errorId: {}, httpMethod: {}, uri: {}", error.getErrorId(),
          error.getHttpMethod(), error.getUri(), exception);
    } else {
      logError(error);
    }
    return ResponseEntity.status(status).body(errors);
  }

  static HttpStatusCode resolveStatus(final RuntimeException exception) {
    if (exception instanceof ErrorResponse errorResponse) {
      return errorResponse.getStatusCode();
    }
    if (exception instanceof HttpMessageNotReadableException
        || exception instanceof TypeMismatchException) {
      return HttpStatus.BAD_REQUEST;
    }
    ResponseStatus responseStatus =
        AnnotatedElementUtils.findMergedAnnotation(exception.getClass(), ResponseStatus.class);
    if (responseStatus != null) {
      return responseStatus.code();
    }
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  private static String reasonPhrase(final HttpStatusCode status) {
    HttpStatus known = HttpStatus.resolve(status.value());
    return known != null ? known.getReasonPhrase() : "Error";
  }

  private static boolean isSecurityException(final RuntimeException exception) {
    for (Class<?> type = exception.getClass(); type != null; type = type.getSuperclass()) {
      String typeName = type.getName();
      if ("org.springframework.security.access.AccessDeniedException".equals(typeName)
          || "org.springframework.security.core.AuthenticationException".equals(typeName)) {
        return true;
      }
    }
    return false;
  }
}
