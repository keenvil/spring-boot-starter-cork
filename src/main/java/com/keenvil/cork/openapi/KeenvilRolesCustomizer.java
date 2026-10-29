package com.keenvil.cork.openapi;

import io.swagger.v3.oas.models.Operation;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

/**
 * Agrega a la descripcion de cada operacion los roles que pide su {@code @PreAuthorize} (directo o por una
 * meta-anotacion como {@code @HasAdminRoleInCommunity}), y la extension {@code x-roles}. Los roles salen de los
 * literales en mayusculas de la expresion ({@code 'ADMIN'}, {@code {'GUARD', 'SUPERVISOR'}}).
 */
class KeenvilRolesCustomizer implements GlobalOperationCustomizer {

  static final String ROLES_MARK = "**Roles:**";

  private static final Pattern ROLE = Pattern.compile("'([A-Z][A-Z_]+)'");

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    if (handlerMethod == null) {
      return operation;
    }
    PreAuthorize pre = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), PreAuthorize.class);
    if (pre == null) {
      pre = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), PreAuthorize.class);
    }
    if (pre == null) {
      return operation;
    }
    Set<String> roles = new LinkedHashSet<>();
    Matcher matcher = ROLE.matcher(pre.value());
    while (matcher.find()) {
      roles.add(matcher.group(1));
    }
    if (roles.isEmpty()) {
      return operation;
    }
    String scope = pre.value().contains("Community") ? " en la comunidad de la URL" : "";
    String line = ROLES_MARK + " " + String.join(", ", roles.stream().map(r -> "`" + r + "`").toList()) + scope + ".";
    String description = operation.getDescription();
    if (description == null || !description.contains(ROLES_MARK)) {
      operation.setDescription(description == null || description.isBlank() ? line : description + "\n\n" + line);
    }
    operation.addExtension("x-roles", roles.stream().toList());
    return operation;
  }

}
