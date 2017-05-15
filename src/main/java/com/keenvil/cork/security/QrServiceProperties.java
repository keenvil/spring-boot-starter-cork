package com.keenvil.cork.security;

import org.springframework.context.annotation.Configuration;

@Configuration
public class QrServiceProperties {

  private String imageType;

  private int imageWidth;

  private int imageHeight;

  private String charset;

  public QrServiceProperties() { }

  /**
   * Creates a new instance of this properties.
   * 
   * @param image Image type.
   * @param width Image Width.
   * @param height Image Height.
   * @param theCharset Charset.
   */
  public QrServiceProperties(
      final String image,
      int width,
      int height,
      String theCharset) {
    imageType = image;
    imageWidth = width;
    imageHeight = height;
    charset = theCharset;
  }

  public String getImageType() {
    return imageType;
  }

  public int getImageWidth() {
    return imageWidth;
  }

  public int getImageHeight() {
    return imageHeight;
  }

  public void setImageType(String theImageType) {
    imageType = theImageType;
  }

  public void setImageWidth(int theImageWidth) {
    imageWidth = theImageWidth;
  }

  public void setImageHeight(int theImageHeight) {
    this.imageHeight = theImageHeight;
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(String theCharset) {
    charset = theCharset;
  }
}
