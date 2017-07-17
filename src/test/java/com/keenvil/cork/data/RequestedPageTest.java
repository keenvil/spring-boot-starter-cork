package com.keenvil.cork.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.easymock.EasyMockRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;

import com.keenvil.cork.data.RequestedPage.Sort;


@RunWith(EasyMockRunner.class)
public class RequestedPageTest {

  @Test
  public void create() throws Exception {
    RequestedPage requestedPage = new RequestedPage(1, 20, Sort.ASC);
    assertThat(requestedPage.getPage(), is(1));
    assertThat(requestedPage.getSize(), is(20));
    assertThat(requestedPage.getSort(), is(Sort.ASC));

    requestedPage.setPage(2);
    requestedPage.setSize(10);
    requestedPage.setSort(Sort.DESC);
    assertThat(requestedPage.getPage(), is(2));
    assertThat(requestedPage.getSize(), is(10));
    assertThat(requestedPage.getSort(), is(Sort.DESC));
    assertThat(Sort.ASC, is(Sort.valueOf("ASC")));
    assertThat(Sort.DESC, is(Sort.valueOf("DESC")));
  }

  @Test
  public void toPage() {
    RequestedPage requestedPage = new RequestedPage(0, 20, Sort.ASC);
    PageRequest pageRequest = requestedPage.toPage("name");

    assertNotNull(pageRequest);
    assertThat(pageRequest.getPageNumber(), is(0));
    assertThat(pageRequest.getPageSize(), is(20));    
  }
}
