package com.keenvil.cork.openapi;

import com.keenvil.cork.data.RequestedPage;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

/**
 * Documenta un {@link RequestedPage} recibido por query como sus tres parametros ({@code page}, {@code size} y
 * {@code sort}) en lugar del objeto obligatorio que infiere springdoc, sin que el servicio tenga que anotarlo
 * con {@code @ParameterObject}. El schema {@code RequestedPage} que queda sin uso lo saca
 * {@link KeenvilOpenApiCustomizer}.
 */
class KeenvilRequestedPageCustomizer implements org.springdoc.core.customizers.GlobalOperationCustomizer {

  static final String PAGE = "page";
  static final String SIZE = "size";
  static final String SORT = "sort";

  private static final ParameterNameDiscoverer NAMES = new DefaultParameterNameDiscoverer();

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    if (handlerMethod == null) {
      return operation;
    }
    Set<String> names = Arrays.stream(handlerMethod.getMethodParameters())
        .filter(parameter -> RequestedPage.class.equals(parameter.getParameterType())
            && !parameter.hasParameterAnnotation(RequestBody.class))
        .map(parameter -> name(parameter, handlerMethod))
        .collect(Collectors.toSet());
    if (names.isEmpty()) {
      return operation;
    }
    List<Parameter> parameters = operation.getParameters() == null ? new ArrayList<>()
        : new ArrayList<>(operation.getParameters().stream()
            .filter(parameter -> !names.contains(parameter.getName()) && !isRequestedPage(parameter)).toList());
    add(parameters, new Parameter().in("query").name(PAGE).required(false)
        .description("Numero de pagina, desde 0.").schema(new IntegerSchema()._default(0).minimum(
            java.math.BigDecimal.ZERO)));
    add(parameters, new Parameter().in("query").name(SIZE).required(false)
        .description("Cantidad de elementos por pagina.").schema(new IntegerSchema()._default(20)));
    StringSchema sort = new StringSchema();
    sort.setEnum(List.of(RequestedPage.Sort.ASC.name(), RequestedPage.Sort.DESC.name()));
    sort.setDefault(RequestedPage.Sort.ASC.name());
    add(parameters, new Parameter().in("query").name(SORT).required(false)
        .description("Direccion del orden: `ASC` o `DESC`. El campo por el que se ordena lo fija la operacion.")
        .schema(sort));
    operation.setParameters(parameters);
    return operation;
  }

  private static void add(List<Parameter> parameters, Parameter parameter) {
    if (parameters.stream().noneMatch(p -> parameter.getName().equals(p.getName()) && "query".equals(p.getIn()))) {
      parameters.add(parameter);
    }
  }

  private static boolean isRequestedPage(Parameter parameter) {
    return parameter.getSchema() != null && parameter.getSchema().get$ref() != null
        && parameter.getSchema().get$ref().endsWith("/" + RequestedPage.class.getSimpleName());
  }

  private static String name(MethodParameter parameter, HandlerMethod handlerMethod) {
    MethodParameter discover = parameter.withContainingClass(handlerMethod.getBeanType());
    discover.initParameterNameDiscovery(NAMES);
    String name = discover.getParameterName();
    return name == null ? "" : name;
  }
}
