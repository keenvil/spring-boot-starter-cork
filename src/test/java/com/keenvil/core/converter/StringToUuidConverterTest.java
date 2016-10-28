package com.keenvil.core.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(EasyMockRunner.class)
public class StringToUuidConverterTest {

  @TestSubject
  private StringToUuidConverter converter = new StringToUuidConverter();

  @Test
  public void convert() {
    UUID uuid = converter.convert("12345678123443211423210987654321");
    assertThat(uuid.toString(), is("12345678-1234-4321-1423-210987654321"));

  }

  @Test
  public void invlidPlainUuid() throws Exception {
    try {
      converter.convert(null);
      fail();
    } catch (NullPointerException exception) {
      assertThat(exception.getMessage(), is("Plain UUID cannot be null."));
    }

    try {
      converter.convert("1");
      fail();
    } catch (IllegalArgumentException exception) {
      assertThat(exception.getMessage(), is("Plain UUID length must be 32."));
    }
  }
}
