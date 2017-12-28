package com.keenvil.cork.totp;

import static org.slf4j.LoggerFactory.getLogger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * This class implements the functionality described in RFC 6238 (TOTP: Time
 * based one-time password algorithm) .
 *
 * <p>This class lets generate a TOTP and authorize a TOTP generated with a
 * particular secret key.</p>
 */
@Component
public class TotpAuthenticator {

  /**
   * Cryptographic hash function used to calculate the HMAC (Hash-based Message
   * Authentication Code). This implementation uses the SHA1 hash function.
   */
  private static final String HMAC_HASH_FUNCTION = "HmacSHA1";

  private static Logger log =
      getLogger(TotpAuthenticator.class);

  private TotpConfigurationProperties properties;

  public TotpAuthenticator(TotpConfigurationProperties configuration) {
    properties = configuration;
  }

  /**
   * Generates the next TOTP for the given secret.
   *
   * @param secret Secret.
   * @return Next TOTP.
   */
  public int next(final String secret) {
    return getTotpPassword(secret);
  }

  /**
   * Authorizes a verification code for a given secret.
   *
   * @param secret The secret.
   * @param verificationCode Verification code to validate.
   * @return whether the verification code is valid or not for the given secret.
   */
  public boolean authorize(String secret, int verificationCode) {
    long time = new Date().getTime();

    if (secret == null) {
      throw new IllegalArgumentException("Secret cannot be null.");
    }

    if (verificationCode <= 0 || verificationCode >= properties.getKeyModulus()) {
      return false;
    }

    return checkCode(secret, verificationCode, time, properties.getWindowSize());
  }

  private int getTotpPassword(String secret) {
    return getTotpPassword(secret, new Date().getTime());
  }

  private int getTotpPassword(String secret, long time) {
    return calculateCode(decodeSecret(secret), getTimeWindowFromTime(time));
  }

  private int calculateCode(byte[] key, long tm) {
    byte[] data = new byte[8];
    long value = tm;

    // Converting the instant of time from the long representation to a
    // big-endian array of bytes (RFC4226, 5.2. Description).
    for (int i = 8; i-- > 0; value >>>= 8) {
      data[i] = (byte) value;
    }

    SecretKeySpec signKey = new SecretKeySpec(key, HMAC_HASH_FUNCTION);

    try {
      Mac mac = Mac.getInstance(HMAC_HASH_FUNCTION);
      mac.init(signKey);
      byte[] hash = mac.doFinal(data);

      // Building the validation code performing dynamic truncation
      // (RFC4226, 5.3. Generating an HOTP value)
      int offset = hash[hash.length - 1] & 0xF;

      long truncatedHash = 0;
      for (int i = 0; i < 4; ++i) {
        truncatedHash <<= 8;
        // Java bytes are signed but we need an unsigned integer:
        // cleaning off all but the LSB.
        truncatedHash |= (hash[offset + i] & 0xFF);
      }

      // Clean bits higher than the 32nd (inclusive) and calculate the
      // module with the maximum validation code value.
      truncatedHash &= 0x7FFFFFFF;
      truncatedHash %= properties.getKeyModulus();

      return (int) truncatedHash;
    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
      throw new RuntimeException();
    }
  }

  private boolean checkCode(String secret, long code, long timestamp,
      int window) {
    byte[] decodedKey = decodeSecret(secret);

    final long timeWindow = getTimeWindowFromTime(timestamp);

    for (int i = -((window - 1) / 2); i <= window / 2; ++i) {
      long hash = calculateCode(decodedKey, timeWindow + i);

      log.debug("Validating Code: {}, vs hash: {} using secret: {}", code, hash, secret);

      if (hash == code) {
        log.debug("TOTP {} Found!", code);
        return true;
      }
    }
    log.debug("TOTP {} Not Found!", code);
    return false;
  }

  private byte[] decodeSecret(String secret) {
    return new Base32().decode(secret);
  }

  private long getTimeWindowFromTime(long time) {
    return time / properties.getTimeStepSizeInMillis();
  }
}
