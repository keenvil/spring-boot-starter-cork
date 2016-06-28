package io.mycommunity.commons.multitenant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.Validate;
import org.springframework.boot.context.properties.ConfigurationProperties;

import io.mycommunity.commons.error.PlatformException;

/** Multi tenant/community datasource properties configuration.
 * 
 * <p>Any number of tenant/communities can be defined, but only one can be
 * set as default.</p>
 */
@ConfigurationProperties(prefix = "multitenancy")
public class MultitenancyConfigurationProperties {

  private List<Tenant> tenants;

  private Tenant defaultTenant;

  public MultitenancyConfigurationProperties() { }

  @PostConstruct
  public void init() {
    List<Tenant> defaults = tenants.stream()
        .filter(tc -> tc.isDefault())
        .collect(Collectors.toCollection(ArrayList::new));

    if (defaults.size() != 1) {
      throw new PlatformException("Only one datasource must be set as default");
    }
    defaultTenant = defaults.get(0);
  }

  public List<Tenant> getTenants() {
    return tenants;
  }

  public void setTenants(List<Tenant> someTenants) {
    Validate.notNull(someTenants, "Tenants cannot be null");
    tenants = someTenants;
  }

  public Tenant getDefaultTenant() {
    return defaultTenant;
  }
}
