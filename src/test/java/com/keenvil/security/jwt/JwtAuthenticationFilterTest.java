package com.keenvil.security.jwt;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.keenvil.security.jwt.JwtAuthenticationFilter;
import com.keenvil.security.jwt.JwtService;

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

    try {
      filter.doFilter(request, response, chain);
      fail();
    } catch (Exception e) {
    }
  }

  @Test
  public void doFilterOk() throws IOException, ServletException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(JwtService.X_AUTHORIZATION,
        jwtService.generate("1", "user@myco.io", Collections.<String> emptySet()));
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    Authentication context =
        SecurityContextHolder.getContext().getAuthentication();
    assertThat(context, notNullValue());
    assertThat(context.getName(), is("user@myco.io"));
  }
}
