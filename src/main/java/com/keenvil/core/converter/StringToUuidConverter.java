package com.keenvil.core.converter;

import org.apache.commons.lang3.Validate;
import org.springframework.core.convert.converter.Converter;

import java.util.UUID;

/**
 * Web parameter converter for plain text UUID to UUID.
 * 
 * <p>Converts String web parameters to UUID. Plain UUID must be 32 characters
 * long and this converter will insert dashes to delimit it as described in
 * {@link UUID#toString()}.</p>
 */
public class StringToUuidConverter implements Converter<String, UUID> {

  @Override
  public UUID convert(String plainUuid) {
    Validate.notEmpty(plainUuid, "Plain UUID cannot be empty.");
    plainUuid = plainUuid.replace("-", "");
    Validate.isTrue(plainUuid.length() == 32, "Plain UUID length must be 32.");
    
    String uuid = plainUuid.substring(0, 8)
        + "-" + plainUuid.substring(8, 12)
        + "-" + plainUuid.substring(12, 16)
        + "-" + plainUuid.substring(16, 20)
        + "-" + plainUuid.substring(20, 32);
    return UUID.fromString(uuid);
  }
}
