package com.newshunt.dataentity.common.model.entity;

import java.io.Serializable;

/**
 * For feedback concern area key-value pair.
 *
 * @author sumedh.tambat
 */
public class FeedbackAreaPair implements Serializable {

  private static final long serialVersionUID = 2L;

  private String key;
  private String value;

  public FeedbackAreaPair() {

  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
