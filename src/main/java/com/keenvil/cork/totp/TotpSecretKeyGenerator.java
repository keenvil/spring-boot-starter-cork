package com.keenvil.cork.totp;

import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

/**
 * Generates a secure random secret key used to generate Time base One
 * Time Passwords.
 * 
 * <p>This class lets create a new 16-bit base32-encoded secret key with
 * the validation code calculated at {@code time = 0} (the UNIX epoch).</p>
 */
@Component
public class TotpSecretKeyGenerator {

  private TotpConfigurationProperties properties;
  
  public TotpSecretKeyGenerator(TotpConfigurationProperties configuration) {
    properties = configuration;
  }

  /**
   * Generates a Secure Random Base32 encoded key.
   * 
   * <p>Generated secret keys will have 16 bytes long with default
   * configuration.</p>
   *  
   * @return a Secure Random Base32 encoded key.
   */
  public String generateKey() {
    int length = properties.getSecretBits() / 8 
        + properties.getScratchCodes() * properties.getBytesPerScratchCode();
    byte[] buffer = new byte[length];

    SecureRandom secureRandom = new SecureRandom();
    secureRandom.nextBytes(buffer);

    byte[] secretKey = Arrays.copyOf(buffer, properties.getSecretBits() / 8);
    String generatedKey = new Base32().encodeToString(secretKey);
    return generatedKey;
  }
}
