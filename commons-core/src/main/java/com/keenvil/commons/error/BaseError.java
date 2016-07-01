package com.keenvil.commons.error;

import org.apache.commons.lang3.Validate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/** Presents any kind of business or validation error that should be informed
 * throw an API service.
 */
@ApiModel
@Deprecated
public class BaseError {

  @ApiModelProperty(required = true, value = "Error Code")
  private String code;

  @ApiModelProperty(required = false, value = "Error description.")
  private String message;

  public BaseError() { }

  public BaseError(final String aCode, final String aMessage) {
    Validate.notEmpty(aCode);
    code = aCode;
    message = aMessage;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
