/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity.model;

import androidx.annotation.Nullable;

import com.newshunt.dataentity.social.entity.AdSpec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a multivalue response having a list of data objects.
 *
 * @param <T> the business model represented as a list of items
 * @author amarjit
 */
public class MultiValueResponse<T> implements Serializable {

  /**
   * represents the total number of data items available, can be null if not
   * available.
   */
  private Integer total;

  /**
   * represents the total number of data items in this response
   */
  private Integer count = 0;

  /**
   * represents the next page url that has the pagination details to fetch next
   * set of data, null if none available for fetching.
   */
  private String nextPageUrl;

  private String nextPageLogic;
  private String nextPageLogicId;
  private boolean nextPageIsRefreshUrl;
  private int pageNumber;
  private String pageCursor;
  private AdSpec adSpec;
  private String stickyBannerLang;

  private String localCookie;
  private String globalCookie;


  /**
   * holds the data items
   */
  private List<T> rows = new ArrayList<T>();

  /**
   * Represents version of the item. It can be null where not applicable.
   */
  private String version;

  private Map<String, String> experiment;

  private String pageUrl;

  private ListingMeta listingMeta;

  private Map<String, String> configurations;

  @Nullable
  private Map<String, String> viewMoreParams = null;


  public MultiValueResponse() {

  }

  public MultiValueResponse(Integer total, Integer count, String nextPageUrl,
                            String nextPageLogic, String nextPageLogicId, int pageNumber,
                            List<T> rows, String version, Map<String, String> experiment,
                            String pageCursor, AdSpec adSpec,String localCookie,
                            String globalCookie, String stickyBannerLang) {
    this.total = total;
    this.count = count;
    this.nextPageUrl = nextPageUrl;
    this.nextPageLogic = nextPageLogic;
    this.nextPageLogicId = nextPageLogicId;
    this.pageNumber = pageNumber;
    this.rows = rows;
    this.version = version;
    this.experiment = experiment;
    this.pageCursor = pageCursor;
    this.adSpec = adSpec;
    this.stickyBannerLang = stickyBannerLang;
    this.localCookie = localCookie;
    this.globalCookie = globalCookie;
  }

  public MultiValueResponse(List<T> rows) {
    this.rows = rows;
    if (rows != null) {
      count = rows.size();
    }
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public String getNextPageUrl() {
    return nextPageUrl;
  }

  public void setNextPageUrl(String nextPageUrl) {
    this.nextPageUrl = nextPageUrl;
  }

  public String getNextPageLogic() {
    return nextPageLogic;
  }

  public void setNextPageLogic(String nextPageLogic) {
    this.nextPageLogic = nextPageLogic;
  }

  public List<T> getRows() {
    return rows;
  }

  public void setRows(List<T> rows) {
    this.rows = rows;

    if (rows != null) {
      setCount(rows.size());
    }
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public AdSpec getAdSpec() {
    return adSpec;
  }

  public void setAdSpec(AdSpec adSpec) {
    this.adSpec = adSpec;
  }

  public String getStickyBannerLang() { return stickyBannerLang;}

  public void addRow(T row) {
    if (null == rows) {
      rows = new ArrayList<T>();
    }
    rows.add(row);
    setCount(getCount() + 1);
  }

  public void addAll(List<T> rows) {
    if (null == rows) {
      return;
    }
    setCount(getCount() + rows.size());

    this.rows.addAll(rows);
  }

  @Override
  public String toString() {
    return new StringBuilder().append(getClass()).append(" [").append("total=")
        .append(total).append(", count=").append(count)
        .append(", nextPageUrl=").append(nextPageUrl).append(", rows=").append(rows).append("]")
        .toString();
  }

  public String getNextPageLogicId() {
    return nextPageLogicId;
  }

  public void setNextPageLogicId(String nextPageLogicId) {
    this.nextPageLogicId = nextPageLogicId;
  }

  public Map<String, String> getExperiment() {
    return experiment;
  }

  public void setExperiment(Map<String, String> experiment) {
    this.experiment = experiment;
  }

  public String getPageUrl() {
    return pageUrl;
  }

  public void setPageUrl(String pageUrl) {
    this.pageUrl = pageUrl;
  }

  public ListingMeta getListingMeta() {
    return listingMeta;
  }

  public boolean isNextPageIsRefreshUrl() {
    return nextPageIsRefreshUrl;
  }

  public String getPageCursor() {
    return pageCursor;
  }

  public void setPageCursor(String pageCursor) {
    this.pageCursor = pageCursor;
  }

  public Map<String, String> getConfigurations() {
    return configurations;
  }

  public void setGlobalCookie(String globalCookie) {
    this.globalCookie = globalCookie;
  }

  @Nullable
  public String getGlobalCookie() {
    return globalCookie;
  }

  @Nullable
  public String getLocalCookie() {
    return localCookie;
  }

  public void setLocalCookie(String localCookie) {
    this.localCookie = localCookie;
  }

  public void setConfigurations(Map<String, String> configurations) {
    this.configurations = configurations;
  }

  @Nullable
  public Map<String, String> getViewMoreParams() {
    return viewMoreParams;
  }

  public void setViewMoreParams(@Nullable Map<String, String> viewMoreParams) {
    this.viewMoreParams = viewMoreParams;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MultiValueResponse<?> that = (MultiValueResponse<?>) o;
    return nextPageIsRefreshUrl == that.nextPageIsRefreshUrl &&
        pageNumber == that.pageNumber &&
        Objects.equals(total, that.total) &&
        Objects.equals(count, that.count) &&
        Objects.equals(nextPageUrl, that.nextPageUrl) &&
        Objects.equals(nextPageLogic, that.nextPageLogic) &&
        Objects.equals(nextPageLogicId, that.nextPageLogicId) &&
        Objects.equals(pageCursor, that.pageCursor) &&
        Objects.equals(adSpec, that.adSpec) &&
        Objects.equals(rows, that.rows) &&
        Objects.equals(version, that.version) &&
        Objects.equals(experiment, that.experiment) &&
        Objects.equals(pageUrl, that.pageUrl) &&
        Objects.equals(listingMeta, that.listingMeta) &&
        Objects.equals(configurations, that.configurations) &&
        Objects.equals(viewMoreParams, that.viewMoreParams)&&
        Objects.equals(localCookie, that.localCookie)&&
        Objects.equals(globalCookie, that.globalCookie)&&
        Objects.equals(stickyBannerLang, that.stickyBannerLang);
  }

  @Override
  public int hashCode() {
    return Objects.hash(total, count, nextPageUrl, nextPageLogic, nextPageLogicId,
        nextPageIsRefreshUrl, pageNumber, pageCursor, adSpec, rows, version, experiment, pageUrl,
        listingMeta, configurations, viewMoreParams,localCookie,globalCookie,stickyBannerLang);
  }
}
