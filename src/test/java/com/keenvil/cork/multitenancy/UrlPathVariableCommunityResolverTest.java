package com.keenvil.cork.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.keenvil.cork.UrlPathVariableCommunityResolver;
import com.keenvil.cork.jwt.JwtTokenHolder;

public class UrlPathVariableCommunityResolverTest {

  private UrlPathVariableCommunityResolver helper =
      new UrlPathVariableCommunityResolver();

  @BeforeEach
  public void setUpDefaultTenant() {
    // defaultTenant() ahora lee el name real del tenant marcado default en vez de un
    // literal hardcodeado -- se llama "default" a proposito para que las assertions
    // existentes (is("default")) seguir siendo validas sin reescribirlas todas.
    Tenant defaultTenant = new Tenant();
    defaultTenant.setName("default");
    defaultTenant.setDefault(true);
    defaultTenant.setJdbcUrl("jdbc:h2:mem:test");
    defaultTenant.setDriverClassName("org.h2.Driver");

    MultitenancyConfigurationProperties properties = new MultitenancyConfigurationProperties();
    properties.setTenants(List.of(defaultTenant));
    properties.init();

    ReflectionTestUtils.setField(helper, "multitenancyProperties", properties);
  }

  @AfterEach
  public void clearHolders() {
    // JwtTokenHolder es un ThreadLocal sin scope de test: sin este cleanup, un test
    // que fija un community deja ese valor filtrado para el resto de la clase, ya
    // que todos corren en el mismo hilo.
    JwtTokenHolder.clear();
    RequestContextHolder.resetRequestAttributes();
  }

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

  @Test
  public void resolveFallsBackToHeldCommunityWhenNoRequestAttributes() {
    // Simula un hilo de scheduler/ejecutor interno sin ninguna request HTTP
    // asociada -- debe caer al community fijado explicitamente en JwtTokenHolder
    // por el propio codigo de fondo, no al tenant por defecto.
    RequestContextHolder.resetRequestAttributes();
    JwtTokenHolder.holdCommunity("async-community");

    assertThat(helper.resolve(), is("async-community"));
  }

  @Test
  public void resolveWithoutRequestAttributesAndWithoutHeldCommunity_returnsDefault() {
    RequestContextHolder.resetRequestAttributes();

    assertThat(helper.resolve(), is("default"));
  }

  @Test
  public void resolveWithoutC_doesNotFallBackToHeldCommunity() {
    // Con una request valida mismo sin /c/{id}/, el tenant por defecto sigue
    // ganando -- el holder solo aplica cuando no hay ninguna request para
    // inspeccionar, para no arrastrar un valor viejo de un uso anterior del hilo.
    JwtTokenHolder.holdCommunity("stale-community");
    MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/primary/something/something");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertThat(helper.resolve(), is("default"));
  }

  @Test
  public void resolveFallsBackToHeldCommunityWhenRequestIsRecycled() {
    // Simula el caso real: un hilo @Async con RequestAttributes propagados por el
    // TaskDecorator, pero cuyo HttpServletRequest ya fue reciclado por el contenedor
    // -tirando excepcion al leerlo- para cuando el hilo async corre (confirmado en
    // produccion de crowd-api, ImageService.uploadAvatars). Debe caer al community
    // fijado explicitamente en JwtTokenHolder, no al tenant por defecto.
    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
    when(attributes.getRequest()).thenThrow(new IllegalStateException("recycled"));
    RequestContextHolder.setRequestAttributes(attributes);

    JwtTokenHolder.holdCommunity("async-community");

    assertThat(helper.resolve(), is("async-community"));
  }

  @Test
  public void resolveFallsBackToDefaultWhenRequestIsRecycledAndNoHeldCommunity() {
    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
    when(attributes.getRequest()).thenThrow(new IllegalStateException("recycled"));
    RequestContextHolder.setRequestAttributes(attributes);

    assertThat(helper.resolve(), is("default"));
  }
}
