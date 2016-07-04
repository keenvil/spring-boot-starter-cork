package com.keenvil.core.error;

import java.util.ArrayList;
import java.util.List;

public class PlatformErrors {

  private List<PlatformError> errors = new ArrayList<PlatformError>();

  public PlatformErrors() { }

  public PlatformErrors(List<PlatformError> someErrors) {
    errors = someErrors;
  }

  public List<PlatformError> getErrors() {
    return errors;
  }

  public void add(PlatformError anError) {
    errors.add(anError);
  }
}
