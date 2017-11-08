package com.keenvil.cork.totp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keenvil.cork.totp")
public class TotpConfigurationProperties {

  /**
   * The number of bits of a secret key in binary form. Since the Base32
   * encoding with 8 bit characters introduces an 160% overhead, we just need
   * 80 bits (10 bytes) to generate a 16 bytes Base32-encoded secret key.
   */
  private int secretBits;

  /**
   * Number of scratch codes to generate during the key generation.
   * We are using Google's default of providing 5 scratch codes.
   */
  private int scratchCodes;

  /**
   * Length in bytes of each scratch code. We're using Google's default of
   * using 4 bytes per scratch code.
   */
  private int bytesPerScratchCode;
  
  /**
   * An integer value representing the number of windows of size
   * timeStepSizeInMillis that are checked during the validation process,
   * to account for differences between the server and the client clocks.
   * The bigger the window, the more tolerant the library code is about
   * clock skews.
   */
  private int windowsSize;

  /**
   * The number of digits in the generated code.
   */
  private double codeDigits;

  /**
   * The time step size, in milliseconds, as specified by RFC 6238.
   * The default value is 30.000.
   */
  private long timeStepSizeInMillis;

  /**
   * Creates a new TOTP configuration.
   * 
   * @param theSecretBits Secret bits.
   * @param theScratchCodes Scratch codes.
   * @param theBytesPerScratchCode Bytes per scratch code.
   * @param theWindowsSize Windows size.
   * @param theCodeDigits Code digits.
   * @param theTimeStepSizeInMillis Time step size in millis
   */
  public TotpConfigurationProperties(
      @Value("${keenvil.cork.totp.secretBits:80}")
      int theSecretBits,
      @Value("${keenvil.cork.totp.scratchCodes:5}")
      int theScratchCodes,
      @Value("${keenvil.cork.totp.bytesPerScratchCode:4}")
      int theBytesPerScratchCode,
      @Value("${keenvil.cork.totp.windowsSize:6}")
      int theWindowsSize,
      @Value("${keenvil.cork.totp.codeDigits:6.0}")
      double theCodeDigits,
      @Value("${keenvil.cork.totp.timeStepSizeInMillis:30000}")
      long theTimeStepSizeInMillis) {
    super();
    secretBits = theSecretBits;
    scratchCodes = theScratchCodes;
    bytesPerScratchCode = theBytesPerScratchCode;
    windowsSize = theWindowsSize;
    codeDigits = theCodeDigits;
    timeStepSizeInMillis = theTimeStepSizeInMillis;
  }

  public int getSecretBits() {
    return secretBits;
  }

  public void setSecretBits(int theSecretBits) {
    secretBits = theSecretBits;
  }

  public int getScratchCodes() {
    return scratchCodes;
  }

  public void setScratchCodes(int theScratchCodes) {
    scratchCodes = theScratchCodes;
  }

  public int getBytesPerScratchCode() {
    return bytesPerScratchCode;
  }

  public void setBytesPerScratchCode(int theBytesPerScratchCode) {
    bytesPerScratchCode = theBytesPerScratchCode;
  }

  public int getWindowSize() {
    return windowsSize;
  }

  public void setWindowsSize(int theWindowsSize) {
    windowsSize = theWindowsSize;
  }

  public double getCodeDigits() {
    return codeDigits;
  }

  public void setCodeDigits(double theCodeDigits) {
    codeDigits = theCodeDigits;
  }

  public long getTimeStepSizeInMillis() {
    return timeStepSizeInMillis;
  }

  public void setTimeStepSizeInMillis(long theTimeStepSizeInMillis) {
    timeStepSizeInMillis = theTimeStepSizeInMillis;
  }

  public int getKeyModulus() {
    return (int) Math.pow(10, getCodeDigits());
  }
}
