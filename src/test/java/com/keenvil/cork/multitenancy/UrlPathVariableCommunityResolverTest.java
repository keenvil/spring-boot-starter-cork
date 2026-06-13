package com.keenvil.cork.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.keenvil.cork.UrlPathVariableCommunityResolver;

public class UrlPathVariableCommunityResolverTest {

  private UrlPathVariableCommunityResolver helper =
      new UrlPathVariableCommunityResolver();

  @Test
  public void resolveTenant() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/c/primary/something/something");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertThat(helper.resolve(), is("primary"));
  }

  @Test
  public void resolveWithoutC() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/primary/something/something");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertThat(helper.resolve(), is("default"));
  }

  @Test
  public void resolveWithoutUri() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertThat(helper.resolve(), is("default"));
  }

  @Test
  public void resolveWithoutCommunityName() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/primary/something/c");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertThat(helper.resolve(), is("default"));
  }
}
