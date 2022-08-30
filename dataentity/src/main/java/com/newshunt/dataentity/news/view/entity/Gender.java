/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.view.entity;

/**
 * Gender for passing in the Astro Subscription API
 * Created by anshul on 17/2/17.
 */

public enum Gender {

  MALE("M"),
  FEMALE("F"),
  OTHER("O");

  private String gender;

  Gender(String gender) {
    this.gender = gender;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public static Gender getGender(String genderStr) {
    for (Gender gender : Gender.values()) {
      if (gender.getGender().equalsIgnoreCase(genderStr)) {
        return gender;
      }
    }
    return null;
  }
}
