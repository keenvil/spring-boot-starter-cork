package com.keenvil.user;

import static com.keenvil.web.security.jwt.JwtAuthenticationFilter.X_JWT_USER;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.keenvil.core.error.PlatformException;
import com.keenvil.web.security.jwt.JwtService.JwtUser;

import org.easymock.TestSubject;
import org.junit.Test;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

public class JwtLoggedUserMethodArgumentResolverTest {

  @TestSubject
  private JwtLoggedUserMethodArgumentResolver resolver =
      new JwtLoggedUserMethodArgumentResolver();

  @Test
  @SuppressWarnings("unchecked")
  public void resolve() throws Exception {
    JwtUser jwtUser = new JwtUser(1L, "user", Collections.EMPTY_SET);
    HttpServletRequest request = createMock(HttpServletRequest.class);
    expect(request.getAttribute(X_JWT_USER)).andReturn(jwtUser);
    
    NativeWebRequest nativeWebRequest = createMock(NativeWebRequest.class);
    expect(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).andReturn(request);
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
    expect(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).andReturn(request);
    replay(request, nativeWebRequest);

    try {
      resolver.resolveArgument(null, null, nativeWebRequest, null);
    } catch (PlatformException.InvalidJwtToken exception) {
      assertThat(exception.getMessage(), is("Json Web Token not found."));
    }
    verify(request, nativeWebRequest);
  }
}
