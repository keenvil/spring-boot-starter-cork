package io.mycommunity.commons.error;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import io.mycommunity.commons.error.BaseError;
import io.mycommunity.commons.error.ErrorTranslator;

@RunWith(EasyMockRunner.class)
public class ErrorTranslatorTest {

  private ErrorTranslator parser = new ErrorTranslator();

  private BindingResult result;

  @Test
  public void parse() {
    result = createMock(BindingResult.class);
    ObjectError error =
        new ObjectError("Visitor", new String[]{"firstName"}, null,
            "Default message");
    List<ObjectError> objectErrors = new ArrayList<>();
    objectErrors.add(error);
    expect(result.getAllErrors()).andReturn(objectErrors);
    replay(result);

    List<BaseError> errors = parser.translate(result);
    assertThat(errors, notNullValue());
    assertThat(errors.get(0).getCode(), is("firstName"));
    assertThat(errors.get(0).getMessage(), is("Default message"));
    verify(result);
  }
}
