package com.keenvil.cork.multitenancy;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.keenvil.cork.UrlPathVariableCommunityResolver;
import com.keenvil.cork.jwt.JwtTokenHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServletRequestAttributes.class)
public class UrlPathVariableCommunityResolverTest {

  @TestSubject
  private UrlPathVariableCommunityResolver helper =
      new UrlPathVariableCommunityResolver();

  @After
  public void clearHolders() {
    // JwtTokenHolder.holdCommunity(...) es un ThreadLocal sin scope de test:
    // sin este cleanup, un test que resuelve "primary" deja ese valor filtrado
    // para el resto de la clase, ya que todos corren en el mismo hilo.
    JwtTokenHolder.holdCommunity(null);
    RequestContextHolder.resetRequestAttributes();
  }

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

  @Test
  public void resolveFallsBackToHeldCommunityWhenRequestIsUnusable() {
    // Simula el caso real: un hilo @Async con RequestAttributes propagados
    // por el TaskDecorator, pero cuyo HttpServletRequest ya fue reciclado
    // por el contenedor -tirando excepcion al leerlo- para cuando el hilo
    // async corre. Debe caer al community fijado explicitamente en
    // JwtTokenHolder, no al tenant por defecto.
    ServletRequestAttributes attributes =
        PowerMock.createMock(ServletRequestAttributes.class);
    expect(attributes.getRequest()).andThrow(new IllegalStateException("recycled"));
    RequestContextHolder.setRequestAttributes(attributes);
    PowerMock.replay(attributes);

    JwtTokenHolder.holdCommunity("async-community");

    String tenant = helper.resolve();
    assertThat(tenant, is("async-community"));
    PowerMock.verify(attributes);
  }

  @Test
  public void resolveFallsBackToDefaultWhenNoRequestAndNoHeldCommunity() {
    RequestContextHolder.resetRequestAttributes();

    String tenant = helper.resolve();
    assertThat(tenant, is("default"));
  }
}
