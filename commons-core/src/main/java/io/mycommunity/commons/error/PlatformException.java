package io.mycommunity.commons.error;

import java.util.List;

import org.springframework.validation.ObjectError;

/** Platform common errors.
 * <p>Platform common errors are application common errors than can be reused
 * across the platform.
 */
@SuppressWarnings("serial")
public class PlatformException extends RuntimeException {

  public PlatformException() { }

  public PlatformException(final String message) {
    super(message);
  }

  /** Used to indicate that a user already exists into the platform.
   */
  public static class UserAlreadyExists extends PlatformException {

    public UserAlreadyExists() { }

    public UserAlreadyExists(final String message) {
      super(message);
    }
  }

  /** Used to indicate that an entity was not found into the platform.
   */
  public static class EntityNotFound extends PlatformException {

    public EntityNotFound() { }

    public EntityNotFound(final String message) {
      super(message);
    }
  }

  /** Used to indicate that there was some validation errors in a API service.
   */
  public static class ValidationError extends PlatformException {

    public List<ObjectError> validationErrors;

    public ValidationError() { }

    public ValidationError(final List<ObjectError> someErrors) {
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

    public Authorization() { }

    public Authorization(final String message) {
      super(message);
    }
  }
}
