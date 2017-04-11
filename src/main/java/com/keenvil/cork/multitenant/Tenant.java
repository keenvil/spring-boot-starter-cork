package com.keenvil.cork.multitenant;

import java.util.Map;

import org.apache.commons.lang3.Validate;

/**
 * Represents a tenant (in platform context a Community).
 * 
 * <p>Each Tenant has its own datasource for each  database schema.</p>
 */
public class Tenant {

  private String name;

  private boolean isDefault;

  private String url;

  private String username;

  private String password;

  private String driverClassName;

  private Map<String, String> dataSourceProperties;

  public Tenant() { }

  public String getName() {
    return name;
  }

  public boolean isDefaultTenant() {
    return isDefault;
  }

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public void setName(String theName) {
    Validate.notBlank(theName, "Name cannot be empty.");
    name = theName;
  }

  public void setUrl(String theUrl) {
    Validate.notBlank(theUrl, "Url cannot be empty.");
    url = theUrl;
  }

  public void setUsername(String theUsername) {
    Validate.notBlank(theUsername, "Username cannot be empty.");
    username = theUsername;
  }

  public void setPassword(String thePassword) {
    password = thePassword;
  }

  public void setDriverClassName(String theDriverClassName) {
    Validate.notBlank(theDriverClassName, "Driver cannot be empty.");
    driverClassName = theDriverClassName;
  }

  public void setDataSourceProperties(Map<String, String> theProperties) {
    dataSourceProperties = theProperties;
  }

  public Map<String, String> getDataSourceProperties() {
    return dataSourceProperties;
  }
}
