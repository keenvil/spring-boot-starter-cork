package io.mycommunity.commons.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import io.mycommunity.commons.error.BaseError;

/** Translates Spring binding results into platform error codes.
 * <p>Platform codes are as following:
 * <ul>
 * <li> Code as error.rejectedEntity.rejectedProperty (Size.userAccount.name)
 * <li> Default Message (username length must be between 5 and 255 characters)
 * </ul>
 */
@Component
public class ErrorTranslator {

  /** Translate Spring binding results into platform specific error codes. 
   * @param result binding errors
   * @return list of platform specific errors
   */
  public List<BaseError> translate(final BindingResult result) {
    
    Validate.notNull(result);
    List<BaseError> fieldErrors = new ArrayList<BaseError>();
    List<ObjectError> allErrors = result.getAllErrors();
    for (ObjectError objectError : allErrors) {
      fieldErrors.add(new BaseError(objectError.getCodes()[0],
          objectError.getDefaultMessage()));
    }
    return fieldErrors;
  }
}
