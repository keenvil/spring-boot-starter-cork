package com.keenvil.cork.multitenancy;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.easymock.EasyMockRunner;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.keenvil.cork.multitenancy.RequestAttributeCommunityResolverHelper;

@RunWith(EasyMockRunner.class)
public class RequestAttributeCommunityResolverHelperTest {

  @TestSubject
  private RequestAttributeCommunityResolverHelper helper =
      new RequestAttributeCommunityResolverHelper();

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
