package com.keenvil.web.security.jwt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.keenvil.web.security.jwt.JwtAuthenticationFilter;
import com.keenvil.web.security.jwt.JwtService;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;

public class JwtAuthenticationFilterTest {

  private JwtService jwtService = new JwtService();
  private JwtAuthenticationFilter filter =
      new JwtAuthenticationFilter(jwtService);

  @Before
  public void setUp() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  public void emptyAuthenticationParameter()
      throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(JwtService.X_AUTHORIZATION, "");
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    Authentication context =
        SecurityContextHolder.getContext().getAuthentication();
    assertThat(context, nullValue());
  }

  @Test
  public void invalidAuthenticationParameter()
      throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(JwtService.X_AUTHORIZATION, "invalid");
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);
    assertThat(SecurityContextHolder.getContext().getAuthentication(),
        nullValue());
  }

  @Test
  public void doFilterOk() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(JwtService.X_AUTHORIZATION,
        jwtService.generate("1", "user@keenvil.com",
            Collections.<String>emptySet()));
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    Authentication context =
        SecurityContextHolder.getContext().getAuthentication();
    assertThat(context, notNullValue());
    assertThat(context.getName(), is("user@keenvil.com"));
  }
}
