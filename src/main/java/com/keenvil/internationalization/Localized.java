package com.keenvil.internationalization;

import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Base class for objects that must be filtered by locale (like messages, tags,
 * etc.).
 */
@MappedSuperclass
public abstract class Localized {

  @Column(name = "locale", nullable = false)
  private  String locale;

  Localized() { }

  public Localized(final String theLocale) {
    Validate.notEmpty("Locale cannot be empty.");
    locale = theLocale;
  }

  public String getLocale() {
    return locale;
  }
}
