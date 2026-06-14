package com.keenvil.cork.multitenancy;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.keenvil.cork.RequestAttributeCommunityResolver;

public class RequestAttributeCommunityResolverTest {

  private RequestAttributeCommunityResolver helper =
      new RequestAttributeCommunityResolver();

  @Test
  public void test() {
    RequestAttributes attributes = createMock(RequestAttributes.class);
    expect(attributes.getAttribute("community-id",
        RequestAttributes.SCOPE_REQUEST)).andReturn("primary");
    RequestContextHolder.setRequestAttributes(attributes);
    replay(attributes);

    String communityId = helper.resolve();
    assertThat(communityId, is("primary"));

    verify(attributes);
  }
}
