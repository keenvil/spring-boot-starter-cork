package com.keenvil.cork.multitenancy;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.keenvil.cork.UrlPathVariableCommunityResolver;
import com.keenvil.cork.multitenancy.CurrentTenantResolver;

@ExtendWith(EasyMockExtension.class)
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
