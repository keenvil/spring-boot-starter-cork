package com.keenvil.cork.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import com.keenvil.cork.error.KeenvilApiException.Authorization;
import com.keenvil.cork.error.KeenvilApiException.BadRequest;
import com.keenvil.cork.error.KeenvilApiException.Forbidden;
import com.keenvil.cork.error.KeenvilApiException.InvalidArgument;
import com.keenvil.cork.error.KeenvilApiException.InvalidResourceState;
import com.keenvil.cork.error.KeenvilApiException.ResourceAlreadyExists;
import com.keenvil.cork.error.KeenvilApiException.ResourceNotFound;
import com.keenvil.cork.error.KeenvilApiException.UnprocessedEntity;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/** Mensaje, codigo y causa de cada constructor de las excepciones de API. */
class KeenvilApiExceptionTest {

  private static final RuntimeException CAUSE = new RuntimeException("cause");

  private static void check(final Supplier<KeenvilApiException> empty,
      final Function<String, KeenvilApiException> withMessage,
      final BiFunction<String, String, KeenvilApiException> withCode,
      final BiFunction<String, Throwable, KeenvilApiException> withCause) {
    assertThat(empty.get().getCode(), is(nullValue()));
    assertThat(withMessage.apply("m").getMessage(), is("m"));
    KeenvilApiException coded = withCode.apply("m", "c");
    assertThat(coded.getMessage(), is("m"));
    assertThat(coded.getCode(), is("c"));
    KeenvilApiException caused = withCause.apply("m", CAUSE);
    assertThat(caused.getMessage(), is("m"));
    assertThat(caused.getCause(), is(sameInstance(CAUSE)));
  }

  @Test
  void base() {
    check(KeenvilApiException::new, KeenvilApiException::new, KeenvilApiException::new,
        KeenvilApiException::new);
  }

  @Test
  void resourceNotFound() {
    check(ResourceNotFound::new, ResourceNotFound::new, ResourceNotFound::new, ResourceNotFound::new);
  }

  @Test
  void resourceAlreadyExists() {
    check(ResourceAlreadyExists::new, ResourceAlreadyExists::new, ResourceAlreadyExists::new,
        ResourceAlreadyExists::new);
  }

  @Test
  void invalidResourceState() {
    check(InvalidResourceState::new, InvalidResourceState::new, InvalidResourceState::new,
        InvalidResourceState::new);
    KeenvilApiError error = new KeenvilApiError.KeenvilApiErrorBuilder().code("x").build();
    assertThat(new InvalidResourceState(List.of(error)).getErrors().get(0).getCode(), is("x"));
  }

  @Test
  void invalidArgument() {
    check(InvalidArgument::new, InvalidArgument::new, InvalidArgument::new, InvalidArgument::new);
  }

  @Test
  void authorization() {
    check(Authorization::new, Authorization::new, Authorization::new, Authorization::new);
  }

  @Test
  void forbidden() {
    check(Forbidden::new, Forbidden::new, Forbidden::new, Forbidden::new);
  }

  @Test
  void unprocessedEntity() {
    check(UnprocessedEntity::new, UnprocessedEntity::new, UnprocessedEntity::new, UnprocessedEntity::new);
  }

  @Test
  void badRequest() {
    check(BadRequest::new, BadRequest::new, BadRequest::new, BadRequest::new);
  }
}
