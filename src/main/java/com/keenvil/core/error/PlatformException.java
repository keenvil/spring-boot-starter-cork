package com.keenvil.core.error;

import org.apache.commons.lang3.Validate;
import org.springframework.validation.ObjectError;

import java.util.List;

/** Platform common errors.
 * <p>Platform common errors are application common errors than can be reused
 * across the platform.</p>
 */
@SuppressWarnings("serial")
public class PlatformException extends RuntimeException {

  public PlatformException() { 
    super();
  }

  public PlatformException(final String message) {
    super(message);
  }

  public PlatformException(final String message, final Throwable throwable) {
    super(message, throwable);
  }

  /** Used to indicate that a user already exists into the platform.
   */
  public static class UserAlreadyExists extends PlatformException {

    public UserAlreadyExists() {
      super();
    }

    public UserAlreadyExists(final String message) {
      super(message);
    }
  }

  /** Used to indicate that an entity was not found into the platform.
   */
  public static class EntityNotFound extends PlatformException {

    public EntityNotFound() {
      super();
    }

    public EntityNotFound(final String message) {
      super(message);
    }
  }

  /** Used to indicate that there was some validation errors in a API service.
   */
  public static class ValidationError extends PlatformException {

    private List<ObjectError> validationErrors ;

    public ValidationError(final List<ObjectError> someErrors) {
      Validate.notNull(someErrors, "Errors cannot be null.");
      validationErrors = someErrors;
    }

    public List<ObjectError> getValidationErrors() {
      return validationErrors;
    }
  }

  /** Used to indicate that there was a problem trying to authenticate a user
   * into the platform.
   */
  public static class Authorization extends PlatformException {

    public Authorization() {
      super();
    }

    public Authorization(final String message) {
      super(message);
    }
  }

  /** Used to indicate that there was a problem identifying a community id.
   */
  public static class InvalidCommunityId extends PlatformException {

    public InvalidCommunityId() {
      super();
    }

    public InvalidCommunityId(final String message) {
      super(message);
    }
  }

  /** Used to indicate that JSON Web Token is not valid.
   */
  public static class InvalidJwtToken extends PlatformException {

    public InvalidJwtToken() {
      super();
    }

    public InvalidJwtToken(final String message) {
      super(message);
    }

    public InvalidJwtToken(final String message, final Throwable throwable) {
      super(message, throwable);
    }
  }
}
