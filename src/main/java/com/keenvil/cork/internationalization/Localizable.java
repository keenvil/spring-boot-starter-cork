package com.keenvil.cork.internationalization;

import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Base class for objects that must be filtered by locale (like messages, tags,
 * etc.).
 * <p>This object is used with {@link Localized} and {@link LocalizableAspect}
 * in order to make a Rest service localizable using an http header value.</p>
 * 
 * <p>To activate this filter and make a Rest service localizable simple:
 * <ul>
 * <li>Make you entity object extend this base class,</li>
 * <li>Annotate your Rest Service with {@link Localized},</li>
 * <li>As service signature add {@link RequestHeader} with X-User-Local
 * value as the first argument (TODO mario: use annotation to get header
 * value).</li>
 * </ul>
 */
@MappedSuperclass
@FilterDef(name = Localizable.FILTER_NAME, parameters = {
      @ParamDef(name = Localizable.FILTER_PARAM, type = String.class)
    })
@Filters({
      @Filter(name = Localizable.FILTER_NAME, condition = ":locale = locale")
    })
public abstract class Localizable {

  public static final String FILTER_NAME = "localizableEntityFilter";

  public static final String FILTER_PARAM = "locale";

  // Definitely not the best place to put this constant. 
  public static final String USER_LOCALE = "X-User-Locale";

  @Column(name = "locale", nullable = false)
  private  String locale;

  public Localizable() { }

  public Localizable(final String theLocale) {
    Validate.notEmpty("Locale cannot be empty.");
    locale = theLocale;
  }

  public String getLocale() {
    return locale;
  }
}
