package com.keenvil.cork.security;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * Service to generate QR codes from given data.
 */
@Service
public class QrService {

  private static final String CHARSET = "UTF-8";

  /**
   * Generates a QR code from the given data encoded with Base64 algorithm.
   * 
   * @param data data to use to generate the QR code.
   * @return QR code as a String.
   */
  public String generateQr(final String data) {
    ByteArrayOutputStream qrCodeFile =
        QRCode.from(data)
        .withCharset(CHARSET)
        .to(ImageType.PNG)
        .stream();
    byte[] encodeBase64 = Base64.encodeBase64(qrCodeFile.toByteArray(), false);
    return StringUtils.newStringUtf8(encodeBase64);
  }
}
