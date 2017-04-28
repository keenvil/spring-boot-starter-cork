package com.keenvil.cork.multitenancy;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.keenvil.cork.UrlPathVariableCommunityResolver;
import com.keenvil.cork.multitenancy.CurrentTenantResolver;

@RunWith(EasyMockRunner.class)
public class CurrentTenantResolverTest {

  @TestSubject
  private CurrentTenantResolver resolver =
      new CurrentTenantResolver();

  @Mock
  private UrlPathVariableCommunityResolver helper;

  @Test
  public void test() {
    expect(helper.resolve()).andReturn("primary");
    replay(helper);

    String tenant = resolver.resolveCurrentTenantIdentifier();
    assertThat(tenant, is("primary"));

    verify(helper);
  }
}
