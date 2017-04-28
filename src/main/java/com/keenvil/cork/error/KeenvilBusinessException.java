package com.keenvil.cork.error;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.validation.ObjectError;

/**
 * Keenvil base exception for business errors.
 */
public class KeenvilBusinessException extends KeenvilException {

  private static final long serialVersionUID = 1L;

  /**
   * Business Validation Error.
   */
  public static class ValidationError extends KeenvilBusinessException {
    
    private static final long serialVersionUID = 1L;

    private List<ObjectError> errors;

    public ValidationError(final List<ObjectError> theErrors) {
      Validate.notNull(theErrors, "Errors cannot be null.");
      errors = theErrors;
    }

    public List<ObjectError> getErrors() {
      return errors;
    }
  }
}
