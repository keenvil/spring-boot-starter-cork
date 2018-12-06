package com.keenvil.cork.data;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;


/**
 * Requested Page Dto.
 *
 * <p>Used to indicate Requested Page information as page number, page size
 * and Sort direction.</p>
 */
public class RequestedPage {

  /**
   * Sort direction.
   */
  public enum Sort {
    ASC,
    DESC
  }

  private Integer page;
  private Integer size;
  private Sort sort;

  /**
   * Default Values.
   */
  RequestedPage() {
    page = 0;
    size = 20;
    sort = Sort.ASC;
  }

  /**
   * Creates a new Page Request.
   *
   * @param thePage      Page number.
   * @param theSize      Page size.
   * @param theDirection Page Sort Direction.
   */
  public RequestedPage(Integer thePage, Integer theSize, Sort theDirection) {
    page = thePage != null ? thePage : 0;
    size = theSize != null ? theSize : 20;
    sort = theDirection != null ? theDirection : Sort.ASC;
  }

  public Integer getPage() {
    return page;
  }

  public Integer getSize() {
    return size;
  }

  public Sort getSort() {
    return sort;
  }

  public void setPage(Integer page) {
    this.page = page != null ? page : 0;
  }

  public void setSize(Integer size) {
    this.size = size != null ? size : 20;

  }

  public void setSort(Sort sort) {
    this.sort = sort != null ? sort : Sort.ASC;
  }

  /**
   * Creates a {@link PageRequest} from the given object.
   *
   * @param properties Sorting property names.
   * @return {@link PageRequest}.
   */
  public PageRequest toPage(String... properties) {
    return PageRequest.of(
      page,
      size,
      Direction.fromString(sort.name()),
      properties);
  }
}
