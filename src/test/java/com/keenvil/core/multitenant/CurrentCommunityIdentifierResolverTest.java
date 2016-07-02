package com.keenvil.core.multitenant;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static com.keenvil.core.multitenant.CurrentCommunityIdentifierResolver.DEFAULT_TENANT;
import static org.easymock.EasyMock.*;

import org.easymock.TestSubject;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.keenvil.core.multitenant.CurrentCommunityIdentifierResolver;

public class CurrentCommunityIdentifierResolverTest {

  @TestSubject
  private CurrentCommunityIdentifierResolver resolver =
      new CurrentCommunityIdentifierResolver();

  @Test
  public void resolveTenant() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("1");
    RequestContextHolder.setRequestAttributes(attributes);
    replay(attributes);

    String tenant = resolver.resolveCurrentTenantIdentifier();
    assertThat(tenant, notNullValue());
    verify( attributes);
  }

  @Test
  public void resolveTenantWithNullAttributes() {
    RequestContextHolder.setRequestAttributes(null);

    String identifier = resolver.resolveCurrentTenantIdentifier();
    assertThat(identifier, is(DEFAULT_TENANT));
  }

  @Test
  public void resolveTenantWithNullTenant() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn(null);
    RequestContextHolder.setRequestAttributes(attributes);
    replay(attributes);

    String identifier = resolver.resolveCurrentTenantIdentifier();
    assertThat(identifier, is(DEFAULT_TENANT));

    verify( attributes);
  }

  @Test
  public void resolveTenantWithEmptyTenant() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("");
    RequestContextHolder.setRequestAttributes(attributes);
    replay(attributes);

    String identifier = resolver.resolveCurrentTenantIdentifier();
    assertThat(identifier, is(DEFAULT_TENANT));

    verify( attributes);
  }
}
