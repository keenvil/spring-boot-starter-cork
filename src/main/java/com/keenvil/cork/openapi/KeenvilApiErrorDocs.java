package com.keenvil.cork.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * Esquema y respuestas de error estandar del documento OpenAPI: el cuerpo que arma
 * {@link com.keenvil.cork.error.KeenvilApiControllerAdvice}, una lista de {@code KeenvilApiError}.
 */
final class KeenvilApiErrorDocs {

  static final String ERROR_SCHEMA = "KeenvilApiError";
  static final String ERRORS_SCHEMA = "KeenvilApiErrors";
  static final String JSON = "application/json";

  /** Codigo HTTP, nombre del componente en {@code components/responses}, descripcion, code y title de ejemplo. */
  record StandardResponse(String status, String name, String description, String code, String title) {

    String ref() {
      return "#/components/responses/" + name;
    }
  }

  static final StandardResponse BAD_REQUEST = new StandardResponse("400", "BadRequest",
      "Pedido mal formado: JSON invalido, parametro con tipo incorrecto o regla de negocio del pedido.",
      "badRequest", "Bad Request");
  static final StandardResponse UNAUTHORIZED = new StandardResponse("401", "Unauthorized",
      "Falta el header `X-Authorization` o el JWT es invalido o esta vencido.", "unauthorized", "Unauthorized");
  static final StandardResponse FORBIDDEN = new StandardResponse("403", "Forbidden",
      "El usuario no tiene el rol o el perfil que pide la operacion en esta comunidad.", "forbidden", "Forbidden");
  static final StandardResponse NOT_FOUND = new StandardResponse("404", "NotFound",
      "No existe el recurso pedido, o el modulo no esta habilitado en la comunidad.", "resourceNotFound",
      "Resource Not Found");
  static final StandardResponse CONFLICT = new StandardResponse("409", "Conflict",
      "El recurso ya existe o cambio desde que se leyo.", "resourceAlreadyExists", "Resource Already Exists");
  static final StandardResponse PRECONDITION_FAILED = new StandardResponse("412", "PreconditionFailed",
      "El recurso no esta en un estado que permita la operacion.", "invalidResourceState", "Invalid Resource State");
  static final StandardResponse UNPROCESSABLE = new StandardResponse("422", "UnprocessableEntity",
      "El cuerpo no pasa la validacion: hay una entrada por campo con error.", "invalidArgument", "Invalid Argument");
  static final StandardResponse SERVER_ERROR = new StandardResponse("500", "InternalServerError",
      "Error inesperado. Reportar el `errorId`: identifica la linea de log del error.", "internalServerError",
      "Internal Server Error");

  static final List<StandardResponse> ALL = List.of(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, CONFLICT,
      PRECONDITION_FAILED, UNPROCESSABLE, SERVER_ERROR);

  private KeenvilApiErrorDocs() { }

  /** Registra el esquema de error y las respuestas estandar en los componentes (sin pisar los del servicio). */
  static void register(Components components, String module) {
    if (components.getSchemas() == null || !components.getSchemas().containsKey(ERROR_SCHEMA)) {
      components.addSchemas(ERROR_SCHEMA, errorSchema());
    }
    if (!components.getSchemas().containsKey(ERRORS_SCHEMA)) {
      components.addSchemas(ERRORS_SCHEMA, new ArraySchema().items(new Schema<>().$ref(ERROR_SCHEMA))
          .description("Cuerpo de toda respuesta de error: una lista con uno o mas errores."));
    }
    for (StandardResponse response : ALL) {
      if (components.getResponses() == null || !components.getResponses().containsKey(response.name())) {
        components.addResponses(response.name(), new ApiResponse().description(response.description())
            .content(errorContent(response, module)));
      }
    }
  }

  /** Contenido JSON con la lista de errores, para respuestas de error que declara el servicio sin cuerpo. */
  static Content errorContent() {
    return new Content().addMediaType(JSON, new MediaType().schema(new Schema<>().$ref(ERRORS_SCHEMA)));
  }

  private static Content errorContent(StandardResponse response, String module) {
    return new Content().addMediaType(JSON, new MediaType().schema(new Schema<>().$ref(ERRORS_SCHEMA))
        .addExamples(response.name(), new Example().value(List.of(
            example(Integer.parseInt(response.status()), response.code(), response.title(), module)))));
  }

  /** Respuesta de los {@link ApiError} de un mismo status: descripcion por codigo y un ejemplo por codigo. */
  static ApiResponse customResponse(int status, List<ApiError> errors, String module) {
    String title = HttpStatus.resolve(status) == null ? "Error" : HttpStatus.resolve(status).getReasonPhrase();
    StringBuilder description = new StringBuilder();
    MediaType media = new MediaType().schema(new Schema<>().$ref(ERRORS_SCHEMA));
    for (ApiError error : errors) {
      description.append(description.isEmpty() ? "" : "\n").append(errors.size() > 1 ? "- " : "")
          .append('`').append(error.code()).append("`: ").append(error.description());
      media.addExamples(error.code(), new Example().summary(error.code())
          .value(List.of(example(status, error.code(), title, module))));
    }
    return new ApiResponse().description(description.toString()).content(new Content().addMediaType(JSON, media));
  }

  private static Map<String, Object> example(int status, String code, String title, String module) {
    Map<String, Object> example = new LinkedHashMap<>();
    example.put("httpStatus", status);
    example.put("code", code);
    example.put("title", title);
    example.put("detail", "Descripcion del error");
    example.put("module", module);
    example.put("uri", "/" + module + "/c/1/recurso");
    example.put("httpMethod", "GET");
    example.put("hostName", "api.keenvil.com");
    example.put("errorId", "3f0c2a4e-8d9b-4a51-9a7e-2a1c5b6d7e8f");
    return example;
  }

  @SuppressWarnings("rawtypes")
  private static Schema errorSchema() {
    return new ObjectSchema()
        .description("Error de la API. El `code` es estable y es el que conviene usar en el cliente; "
            + "`title` y `detail` son texto para personas.")
        .addProperty("httpStatus", new IntegerSchema().description("Codigo HTTP de la respuesta.").example(404))
        .addProperty("code", new StringSchema()
            .description("Codigo del error: `resourceNotFound`, `unauthorized`, `forbidden`, `badRequest`, "
                + "`invalidArgument`, `resourceAlreadyExists`, `invalidResourceState`, `internalServerError` o "
                + "uno propio del servicio (p. ej. `amount.changed`). En validaciones, el codigo del campo.")
            .example("resourceNotFound"))
        .addProperty("title", new StringSchema().description("Titulo del tipo de error.").example("Resource Not Found"))
        .addProperty("detail", new StringSchema().description("Detalle legible del error."))
        .addProperty("module", new StringSchema().description("Servicio que respondio (`spring.application.name`)."))
        .addProperty("uri", new StringSchema().description("Ruta del pedido."))
        .addProperty("httpMethod", new StringSchema().description("Metodo del pedido.").example("GET"))
        .addProperty("hostName", new StringSchema().description("Host del pedido."))
        .addProperty("errorId", new StringSchema().format("uuid")
            .description("Id unico del error; esta tambien en el log del servicio."));
  }
}
