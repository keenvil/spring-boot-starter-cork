package com.keenvil.cork.multitenancy;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Registers {@link MultitenancySpecification} bean dynamically.
 * 
 * <p>Gets attributes defined dynamically in {@link EnableMultitenancy}
 * annotation and registers its values in {@link MultitenancySpecification}
 * bean so they can be used by {@link MultitenancyAutoConfiguration}.</p>
 */
public class MultitenancyRegistrar implements ImportBeanDefinitionRegistrar {

  private static Logger log = getLogger(MultitenancyRegistrar.class);

  @Override
  public void registerBeanDefinitions(AnnotationMetadata metadata,
      BeanDefinitionRegistry registry) {
    log.trace("Registering Multi Tenancy Specification.");

    Map<String, Object> defaultAttrs = metadata
        .getAnnotationAttributes(EnableMultitenancy.class.getName(), true);

    BeanDefinitionBuilder builder = BeanDefinitionBuilder
        .genericBeanDefinition(MultitenancySpecification.class);
    Object basePackages = defaultAttrs.get("basePackages");
    if (basePackages == null) {
      throw new RuntimeException("Base Packages cannot be empty.");
    }

    if (log.isDebugEnabled()) {
      log.debug("Adding base packages to specification {}.", basePackages);
    }

    builder.addConstructorArgValue(basePackages);
    registry.registerBeanDefinition("multitenancySpecification",
        builder.getBeanDefinition());
  }
}
