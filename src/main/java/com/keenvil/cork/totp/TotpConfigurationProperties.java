package com.keenvil.cork.totp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ConfigurationProperties de estilo JavaBean (sin constructor parametrizado)
 * a proposito: un constructor con {@code @Value(...:default)} en una clase
 * {@code @ConfigurationProperties} dispara el constructor-binding estricto
 * de Boot 3 (automatico para @EnableConfigurationProperties con un unico
 * constructor no-default), que resuelve los parametros via Binder y NO
 * evalua los defaults SpEL de @Value -- deja el default de Java (0/0.0) en
 * cualquier propiedad que nadie configura (que es el caso real: ningun
 * repo del org define keenvil.cork.totp.* en su application.yml). Eso
 * generaba secretBits=0 -> longitud de secreto TOTP 0 -> "Secret cannot
 * be empty" al crear un visitante (crowd-api). El binding JavaBean (campos
 * con su valor por default, sin constructor explicito) preserva el
 * comportamiento real de siempre.
 */
@ConfigurationProperties(prefix = "keenvil.cork.totp")
public class TotpConfigurationProperties {

  /**
   * The number of bits of a secret key in binary form. Since the Base32
   * encoding with 8 bit characters introduces an 160% overhead, we just need
   * 80 bits (10 bytes) to generate a 16 bytes Base32-encoded secret key.
   */
  private int secretBits = 80;

  /**
   * Number of scratch codes to generate during the key generation.
   * We are using Google's default of providing 5 scratch codes.
   */
  private int scratchCodes = 5;

  /**
   * Length in bytes of each scratch code. We're using Google's default of
   * using 4 bytes per scratch code.
   */
  private int bytesPerScratchCode = 4;

  /**
   * An integer value representing the number of windows of size
   * timeStepSizeInMillis that are checked during the validation process,
   * to account for differences between the server and the client clocks.
   * The bigger the window, the more tolerant the library code is about
   * clock skews.
   */
  private int windowsSize = 6;

  /**
   * The number of digits in the generated code.
   */
  private double codeDigits = 6.0;

  /**
   * The time step size, in milliseconds, as specified by RFC 6238.
   * The default value is 30.000.
   */
  private long timeStepSizeInMillis = 30000;

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
