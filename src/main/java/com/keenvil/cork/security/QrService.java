package com.keenvil.cork.security;

import java.io.ByteArrayOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keenvil.cork.CorkConfigurationProperties;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

/**
 * Service to generate QR codes from given data.
 */
@Service
public class QrService {

  @Autowired
  private CorkConfigurationProperties properties;

  /**
   * Generates a QR code from the given data encoded with Base64 algorithm.
   * 
   * @param data data to use to generate the QR code.
   * @return QR code as a String.
   */
  public String generateQr(final String data) {
    QrServiceProperties qrGenerator = properties.getQrGenerator();
    ImageType type = ImageType.valueOf(qrGenerator.getImageType());
    ByteArrayOutputStream qrCodeFile =
        QRCode.from(data)
        .withCharset(qrGenerator.getCharset())
        .withSize(qrGenerator.getImageWidth(), qrGenerator.getImageHeight())
        .to(type)
        .stream();
    byte[] encodeBase64 = Base64.encodeBase64(qrCodeFile.toByteArray(), false);
    return StringUtils.newStringUtf8(encodeBase64);
  }
}
