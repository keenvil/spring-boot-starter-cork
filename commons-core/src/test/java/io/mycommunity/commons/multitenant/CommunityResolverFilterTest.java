package io.mycommunity.commons.multitenant;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import javax.servlet.http.HttpServletRequest;

import org.easymock.TestSubject;
import org.junit.Test;

import io.mycommunity.commons.error.PlatformException;

public class CommunityResolverFilterTest {

  @TestSubject
  private CommunityResolverFilter filter = new CommunityResolverFilter();

  private HttpServletRequest request;

  @Test
  public void doFilterInternal() throws Exception {
    request = createMock(HttpServletRequest.class);
    expect(request.getHeader(CommunityResolverFilter.X_COMMUNITY_ID))
        .andReturn("1");
    request.setAttribute("community-id", "1");
    replay(request);

    filter.preHandle(request, null, null);
    verify(request);
  }

  @Test
  public void doFilterinWihtoutCommunityHeader()
      throws Exception {
    request = createMock(HttpServletRequest.class);
    expect(request.getHeader(CommunityResolverFilter.X_COMMUNITY_ID))
        .andReturn(null);
    replay(request);

    try {
      filter.preHandle(request, null, null);
      fail();
    } catch (PlatformException.InvalidCommunityId e) {
    }
    verify(request);
  }

  @Test
  public void doFilterinWihtoutCommunityHeaderValue()
      throws Exception {
    request = createMock(HttpServletRequest.class);
    expect(request.getHeader(CommunityResolverFilter.X_COMMUNITY_ID))
        .andReturn("");
    replay(request);

    try {
      filter.preHandle(request, null, null);
      fail();
    } catch (PlatformException.InvalidCommunityId e) {
    }
    verify(request);
  }
}
