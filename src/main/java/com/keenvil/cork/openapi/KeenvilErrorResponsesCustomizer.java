package com.keenvil.cork.openapi;

import static com.keenvil.cork.openapi.KeenvilApiErrorDocs.BAD_REQUEST;
import static com.keenvil.cork.openapi.KeenvilApiErrorDocs.FORBIDDEN;
import static com.keenvil.cork.openapi.KeenvilApiErrorDocs.NOT_FOUND;
import static com.keenvil.cork.openapi.KeenvilApiErrorDocs.SERVER_ERROR;
import static com.keenvil.cork.openapi.KeenvilApiErrorDocs.UNAUTHORIZED;
import static com.keenvil.cork.openapi.KeenvilApiErrorDocs.UNPROCESSABLE;

import com.keenvil.cork.openapi.KeenvilApiErrorDocs.StandardResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;

/**
 * Agrega a cada operacion las respuestas de error estandar de cork, sin pisar las que declara la operacion:
 * <ul>
 *   <li>400 si la operacion recibe parametros o cuerpo (JSON invalido, tipo incorrecto).</li>
 *   <li>401 y 403 si la operacion tiene seguridad; una operacion publica declara {@code @SecurityRequirements()}
 *   vacio y no las lleva.</li>
 *   <li>404 si la ruta tiene variables ({@code /c/{id}/...}: recurso inexistente o modulo no habilitado).</li>
 *   <li>422 si recibe cuerpo (validacion de campos).</li>
 *   <li>500 siempre.</li>
 * </ul>
 * Al path variable {@code id} sin descripcion le pone la de la comunidad.
 * 409, 412 y los codigos propios dependen de la operacion: se declaran con {@link ApiError}, que pisa la
 * respuesta estandar del mismo status. A una respuesta 4xx/5xx declarada con {@code @ApiResponse} sin cuerpo, o
 * con el cuerpo que springdoc infiere del metodo ({@code *}{@code /*}), se le pone el esquema de error.
 */
class KeenvilErrorResponsesCustomizer implements GlobalOperationCustomizer {

  static final String COMMUNITY_ID = "id";
  static final String COMMUNITY_ID_DESCRIPTION = "Id de la comunidad (barrio) en `/c/{id}/...`.";

  private final String module;

  KeenvilErrorResponsesCustomizer(String theModule) {
    module = theModule;
  }

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    ApiResponses responses = operation.getResponses();
    if (responses == null) {
      responses = new ApiResponses();
      operation.setResponses(responses);
    }
    responses.forEach((status, response) -> {
      if (isError(status) && response.get$ref() == null && (response.getContent() == null
          || response.getContent().keySet().stream().allMatch("*/*"::equals))) {
        response.setContent(KeenvilApiErrorDocs.errorContent());
      }
    });
    if (handlerMethod != null) {
      Map<Integer, List<ApiError>> custom = new TreeMap<>();
      for (ApiError error : AnnotatedElementUtils.findMergedRepeatableAnnotations(handlerMethod.getMethod(),
          ApiError.class, ApiErrors.class)) {
        custom.computeIfAbsent(error.status(), status -> new ArrayList<>()).add(error);
      }
      ApiResponses target = responses;
      custom.forEach((status, errors) ->
          target.addApiResponse(String.valueOf(status), KeenvilApiErrorDocs.customResponse(status, errors, module)));
    }
    if (operation.getParameters() != null) {
      operation.getParameters().stream()
          .filter(parameter -> COMMUNITY_ID.equals(parameter.getName()) && "path".equals(parameter.getIn())
              && parameter.getDescription() == null)
          .forEach(parameter -> parameter.setDescription(COMMUNITY_ID_DESCRIPTION));
    }
    boolean hasBody = operation.getRequestBody() != null;
    if (hasBody || (operation.getParameters() != null && !operation.getParameters().isEmpty())) {
      add(responses, BAD_REQUEST);
    }
    if (operation.getSecurity() == null || !operation.getSecurity().isEmpty()) {
      add(responses, UNAUTHORIZED);
      add(responses, FORBIDDEN);
    }
    if (hasPathVariable(handlerMethod)) {
      add(responses, NOT_FOUND);
    }
    if (hasBody) {
      add(responses, UNPROCESSABLE);
    }
    add(responses, SERVER_ERROR);
    return operation;
  }

  private static void add(ApiResponses responses, StandardResponse standard) {
    if (!responses.containsKey(standard.status())) {
      responses.addApiResponse(standard.status(), new ApiResponse().$ref(standard.ref()));
    }
  }

  private static boolean isError(String status) {
    return status.startsWith("4") || status.startsWith("5");
  }

  private static boolean hasPathVariable(HandlerMethod handlerMethod) {
    return handlerMethod != null && Arrays.stream(handlerMethod.getMethodParameters())
        .anyMatch(parameter -> parameter.hasParameterAnnotation(PathVariable.class));
  }
}
