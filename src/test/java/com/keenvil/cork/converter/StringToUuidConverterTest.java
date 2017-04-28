package com.keenvil.cork.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.keenvil.cork.converter.StringToUuidConverter;

import java.util.UUID;

@RunWith(EasyMockRunner.class)
public class StringToUuidConverterTest {

  private static final String STRING_UUID = "12345678123443211423210987654321";

  private static final String STRING_UUID_WITH_DASHES =
      "12345678-1234-4321-1423-210987654321";

  @TestSubject
  private StringToUuidConverter converter = new StringToUuidConverter();

  @Test
  public void convert() {
    UUID uuid = converter.convert(STRING_UUID);
    assertThat(uuid.toString(), is(STRING_UUID_WITH_DASHES));
    uuid = converter.convert(STRING_UUID_WITH_DASHES);
    assertThat(uuid.toString(), is(STRING_UUID_WITH_DASHES));
  }

  @Test
  public void invalidPlainUuid() throws Exception {
    try {
      converter.convert(null);
      fail();
    } catch (NullPointerException exception) {
      assertThat(exception.getMessage(), is("Plain UUID cannot be empty."));
    }

    try {
      converter.convert("1");
      fail();
    } catch (IllegalArgumentException exception) {
      assertThat(exception.getMessage(), is("Plain UUID length must be 32."));
    }
  }
}
