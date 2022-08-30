/*
* Copyright (c) 2016 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.asset;

import java.io.Serializable;

/**
 * @author raunak.yadav
 */
public class DialogDetail implements Serializable {
  private static final long serialVersionUID = -3961648243807944732L;

  private String tag;
  private String title;
  private String message;
  private String positiveButtonText;
  private String negativeButtonText;

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPositiveButtonText() {
    return positiveButtonText;
  }

  public void setPositiveButtonText(String positiveButtonText) {
    this.positiveButtonText = positiveButtonText;
  }

  public String getNegativeButtonText() {
    return negativeButtonText;
  }

  public void setNegativeButtonText(String negativeButtonText) {
    this.negativeButtonText = negativeButtonText;
  }

}