package com.keenvil.cork.multitenancy.testsupport;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Trivial entity so {@link com.keenvil.cork.multitenancy.MultitenancyAutoConfiguration}
 * has something real to scan when building the multi-tenant EntityManagerFactory in
 * {@link com.keenvil.cork.multitenancy.MultitenancyAutoConfigurationIT}.
 */
@Entity
public class DummyEntity {

  @Id
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(final Long theId) {
    id = theId;
  }
}
