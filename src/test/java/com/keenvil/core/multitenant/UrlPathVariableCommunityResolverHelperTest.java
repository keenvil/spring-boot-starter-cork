package com.keenvil.core.multitenant;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.easymock.EasyMock.*;

import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.keenvil.web.security.service.UrlPathVariableCommunityResolverHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServletRequestAttributes.class)
public class UrlPathVariableCommunityResolverHelperTest {

  @TestSubject
  private UrlPathVariableCommunityResolverHelper helper =
    new UrlPathVariableCommunityResolverHelper();

  @Test
  public void resolveTenant() {
    ServletRequestAttributes attributes =
        PowerMock.createMock(ServletRequestAttributes.class);
    MockHttpServletRequest request =
          new MockHttpServletRequest("GET", "/c/primary/something/something");
    expect(attributes.getRequest()).andReturn(request);
    RequestContextHolder.setRequestAttributes(attributes);
    PowerMock.replay(attributes);

    String tenant = helper.resolve();
    assertThat(tenant, is("primary"));
    PowerMock.verify(attributes);
  }

  @Test
  public void resolveWithoutC() {
    ServletRequestAttributes attributes =
        PowerMock.createMock(ServletRequestAttributes.class);
    MockHttpServletRequest request =
          new MockHttpServletRequest("GET", "/primary/something/something");
    expect(attributes.getRequest()).andReturn(request);
    RequestContextHolder.setRequestAttributes(attributes);
    PowerMock.replay(attributes);

    String tenant = helper.resolve();
    assertThat(tenant, is("default"));
    PowerMock.verify(attributes);
  }

  @Test
  public void resolveWithoutUri() {
    ServletRequestAttributes attributes =
        PowerMock.createMock(ServletRequestAttributes.class);
    MockHttpServletRequest request =
          new MockHttpServletRequest("GET", "");
    expect(attributes.getRequest()).andReturn(request);
    RequestContextHolder.setRequestAttributes(attributes);
    PowerMock.replay(attributes);

    String tenant = helper.resolve();
    assertThat(tenant, is("default"));
    PowerMock.verify(attributes);
  }

  @Test
  public void resolveWithoutCommunityName() {
    ServletRequestAttributes attributes =
        PowerMock.createMock(ServletRequestAttributes.class);
    MockHttpServletRequest request =
          new MockHttpServletRequest("GET", "/primary/something/c");
    expect(attributes.getRequest()).andReturn(request);
    RequestContextHolder.setRequestAttributes(attributes);
    PowerMock.replay(attributes);

    String tenant = helper.resolve();
    assertThat(tenant, is("default"));
    PowerMock.verify(attributes);
  }
}
