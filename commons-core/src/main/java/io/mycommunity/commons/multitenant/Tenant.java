package io.mycommunity.commons.multitenant;

import org.apache.commons.lang3.Validate;

/** Represents a tenant/community in the platform.
 * 
 * <p>Each Tenant has its own datasource to a different database schema.</p>
 */
public class Tenant {

  private String name;

  private boolean isDefault;

  private String url;

  private String username;

  private String password;

  private String driverClassName;

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

  public void setName(String aName) {
    Validate.notBlank(aName, "Name cannot be empty.");
    name = aName;
  }

  public void setUrl(String aUrl) {
    Validate.notBlank(aUrl, "Url cannot be empty.");
    url = aUrl;
  }

  public void setUsername(String aUsername) {
    Validate.notBlank(aUsername, "Username cannot be empty.");
    username = aUsername;
  }

  public void setPassword(String aPassword) {
    password = aPassword;
  }

  public void setDriverClassName(String aDriverClassName) {
    Validate.notBlank(aDriverClassName, "Driver cannot be empty.");
    driverClassName = aDriverClassName;
  }
}
