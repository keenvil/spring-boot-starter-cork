package com.keenvil.cork.internationalization;

import static org.slf4j.LoggerFactory.getLogger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Aspect to make Rest Service call localizable. Please refere to
 * {@link Localizable} for further information.
 */
@Aspect
@Component
public class LocalizableAspect {

  private static Logger log =  getLogger(LocalizableAspect.class);

  @PersistenceContext
  private EntityManager entityManager;

  /** Aspect to localize a service call.
   * <p>This aspects enables the {@link Localizable#FILTER_NAME}</p> and
   * sets its value with the first argument of the advised call.
   * @param pointcut point cut.
   * @param localized localized annotation.
   */
  @Before("@annotation(localized)")
  public void localize(JoinPoint pointcut, Localized localized) {
    String locale = (String) pointcut.getArgs()[0];

    if (log.isDebugEnabled()) {
      log.debug("Localizing service call {} with {}",
          pointcut.toShortString(), locale);
    }
    getSession()
      .enableFilter(Localizable.FILTER_NAME)
      .setParameter(Localizable.FILTER_PARAM, locale);
  }

  private Session getSession() {
    Session session = entityManager.unwrap(Session.class);
    return session;
  }
}
