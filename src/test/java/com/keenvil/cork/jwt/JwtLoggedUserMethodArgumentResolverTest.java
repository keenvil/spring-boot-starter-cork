package com.keenvil.cork.jwt;

import static com.keenvil.cork.jwt.JwtAuthenticationFilter.X_JWT_USER;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.NativeWebRequest;

import com.keenvil.cork.jwt.JwtInvalidTokenException;
import com.keenvil.cork.jwt.JwtLoggedUserMethodArgumentResolver;
import com.keenvil.cork.jwt.JwtUser;

public class JwtLoggedUserMethodArgumentResolverTest {

  private JwtLoggedUserMethodArgumentResolver resolver =
      new JwtLoggedUserMethodArgumentResolver();

  @Test
  @SuppressWarnings("unchecked")
  public void resolve() throws Exception {
    JwtUser jwtUser = new JwtUser(1L,  "Joe", "Average", "B-52", "user",
        Collections.EMPTY_SET);
    HttpServletRequest request = createMock(HttpServletRequest.class);
    expect(request.getAttribute(X_JWT_USER)).andReturn(jwtUser);

    NativeWebRequest nativeWebRequest = createMock(NativeWebRequest.class);
    expect(nativeWebRequest.getNativeRequest(HttpServletRequest.class))
      .andReturn(request);
    replay(request, nativeWebRequest);

    JwtUser resolveArgument =
        (JwtUser) resolver.resolveArgument(null, null, nativeWebRequest, null);
    assertThat(jwtUser, is(resolveArgument));
    verify(request, nativeWebRequest);
  }

  @Test
  public void resolveAttributeNotFound() throws Exception {
    HttpServletRequest request = createMock(HttpServletRequest.class);
    expect(request.getAttribute(X_JWT_USER)).andReturn(null);

    NativeWebRequest nativeWebRequest = createMock(NativeWebRequest.class);
    expect(nativeWebRequest.getNativeRequest(HttpServletRequest.class))
      .andReturn(request);
    replay(request, nativeWebRequest);

    try {
      resolver.resolveArgument(null, null, nativeWebRequest, null);
    } catch (JwtInvalidTokenException exception) {
      assertThat(exception.getMessage(), is("Json Web Token not found."));
    }
    verify(request, nativeWebRequest);
  }
}
