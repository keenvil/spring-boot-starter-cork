package com.keenvil.cork.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.keenvil.cork.RequestAttributeCommunityResolver;
import com.keenvil.cork.UrlPathVariableCommunityResolver;
import com.keenvil.cork.jwt.JwtTokenHolder;

/**
 * Reproduce el NPE de produccion de crowd-api: una tarea @Async que resuelve el tenant
 * despues de que la request que la encolo ya termino.
 */
public class TenantPropagatingTaskDecoratorTest {

  private final UrlPathVariableCommunityResolver resolver =
      new UrlPathVariableCommunityResolver();

  private final ExecutorService pool = Executors.newSingleThreadExecutor();

  @BeforeEach
  public void setUpDefaultTenant() {
    Tenant defaultTenant = new Tenant();
    defaultTenant.setName("default");
    defaultTenant.setDefault(true);
    defaultTenant.setJdbcUrl("jdbc:h2:mem:test");
    defaultTenant.setDriverClassName("org.h2.Driver");
    MultitenancyConfigurationProperties properties = new MultitenancyConfigurationProperties();
    properties.setTenants(List.of(defaultTenant));
    properties.init();
    ReflectionTestUtils.setField(resolver, "multitenancyProperties", properties);
  }

  @AfterEach
  public void cleanUp() {
    JwtTokenHolder.clear();
    RequestContextHolder.resetRequestAttributes();
    pool.shutdownNow();
  }

  @Test
  public void completedRequestIsNeverRead_resolverUsesHeldCommunity() {
    // Request ya completada cuya HttpServletRequest el contenedor recicla:
    // leerla es justo lo que tiraba NPE en produccion.
    HttpServletRequest recycled = mock(HttpServletRequest.class);
    when(recycled.getRequestURI()).thenThrow(new NullPointerException("recycled"));
    ServletRequestAttributes attributes = new ServletRequestAttributes(recycled);
    attributes.requestCompleted();
    RequestContextHolder.setRequestAttributes(attributes);
    JwtTokenHolder.holdCommunity("primary");

    assertThat(resolver.resolve(), is("primary"));
    verify(recycled, never()).getRequestURI();
  }

  @Test
  public void completedRequestReusedForAnotherCommunity_doesNotLeakThatTenant() {
    // Peor caso: el contenedor ya reutilizo la request para otra comunidad.
    ServletRequestAttributes attributes = new ServletRequestAttributes(
        new MockHttpServletRequest("GET", "/crowd/c/otra-comunidad/visitors"));
    attributes.requestCompleted();
    RequestContextHolder.setRequestAttributes(attributes);
    JwtTokenHolder.holdCommunity("primary");

    assertThat(resolver.resolve(), is("primary"));
  }

  @Test
  public void completedRequestWithoutHeldCommunity_returnsDefault() {
    ServletRequestAttributes attributes = new ServletRequestAttributes(
        new MockHttpServletRequest("GET", "/crowd/c/primary/visitors"));
    attributes.requestCompleted();
    RequestContextHolder.setRequestAttributes(attributes);

    assertThat(resolver.resolve(), is("default"));
  }

  @Test
  public void asyncTaskRunningAfterRequestCompleted_getsTenantAndTokenOfOriginalRequest()
      throws Exception {
    ServletRequestAttributes attributes = new ServletRequestAttributes(
        new MockHttpServletRequest("POST", "/crowd/c/primary/visitors"));
    RequestContextHolder.setRequestAttributes(attributes);
    JwtTokenHolder.holdToken("request-token");

    AtomicReference<String> tenantInTask = new AtomicReference<>();
    AtomicReference<String> tokenInTask = new AtomicReference<>();
    Runnable task = new TenantPropagatingTaskDecorator(resolver).decorate(() -> {
      tenantInTask.set(resolver.resolve());
      tokenInTask.set(JwtTokenHolder.token());
    });

    // La respuesta sale y la request termina antes de que el hilo del pool corra.
    attributes.requestCompleted();
    pool.submit(task).get();

    assertThat(tenantInTask.get(), is("primary"));
    assertThat(tokenInTask.get(), is("request-token"));
  }

  @Test
  public void poolThreadIsLeftClean_nothingLeaksToTheNextTask() throws Exception {
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(
        new MockHttpServletRequest("POST", "/crowd/c/primary/visitors")));
    JwtTokenHolder.holdToken("request-token");
    pool.submit(new TenantPropagatingTaskDecorator(resolver).decorate(() -> { })).get();

    AtomicReference<String> communityAfter = new AtomicReference<>("not-read");
    AtomicReference<String> tokenAfter = new AtomicReference<>("not-read");
    pool.submit(() -> {
      communityAfter.set(JwtTokenHolder.community());
      tokenAfter.set(JwtTokenHolder.token());
    }).get();

    assertThat(communityAfter.get(), nullValue());
    assertThat(tokenAfter.get(), nullValue());
  }

  @Test
  public void callerRunsInRequestThread_restoresTheRequestHolder() {
    // CallerRunsPolicy: la tarea corre en el propio hilo de la request; al terminar,
    // la request tiene que seguir con su token y su community.
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(
        new MockHttpServletRequest("POST", "/crowd/c/primary/visitors")));
    JwtTokenHolder.holdToken("request-token");
    JwtTokenHolder.holdCommunity("primary");

    new TenantPropagatingTaskDecorator(resolver).decorate(() -> { }).run();

    assertThat(JwtTokenHolder.token(), is("request-token"));
    assertThat(JwtTokenHolder.community(), is("primary"));
  }

  @Test
  public void requestAttributeResolver_withoutRequestOrCompleted_usesHeldCommunity() {
    RequestAttributeCommunityResolver attributeResolver = new RequestAttributeCommunityResolver();
    MultitenancyConfigurationProperties properties =
        (MultitenancyConfigurationProperties) ReflectionTestUtils.getField(
            resolver, "multitenancyProperties");
    ReflectionTestUtils.setField(attributeResolver, "multitenancyProperties", properties);

    // Sin request: antes era NPE sobre attributes.
    assertThat(attributeResolver.resolve(), is("default"));

    ServletRequestAttributes attributes =
        new ServletRequestAttributes(new MockHttpServletRequest());
    attributes.requestCompleted();
    RequestContextHolder.setRequestAttributes(attributes);
    JwtTokenHolder.holdCommunity("primary");
    // Request completada: antes era IllegalStateException.
    assertThat(attributeResolver.resolve(), is("primary"));
  }
}
