package com.keenvil.cork.data;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

import java.util.Objects;

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
   * @param thePage Page number.
   * @param theSize Page size.
   * @param theDirection Page Sort Direction.
   */
  public RequestedPage(Integer thePage, Integer theSize, Sort theDirection) {
    page = thePage;
    size = theSize;
    sort = theDirection;
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
    this.page = page;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public void setSort(Sort sort) {
    this.sort = sort;
  }

  /**
   * Creates a {@link PageRequest} from the given object.
   * 
   * @param properties Sorting property names.
   * @return {@link PageRequest}.
   */
  public PageRequest toPage(String... properties) {
    PageRequest pageRequest = new PageRequest(
        page,
        size,
      Objects.requireNonNull(Direction.fromOptionalString(sort.name()).orElse(null)),
        properties);
    return pageRequest;
  }
}
