/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.social.entity;

/**
 * @author shrikant on 21/03/18.
 * Like type for the entities
 */
public enum  LikeType {

  //TODO[umesh.isra] - Remove intb values once Buzz like api is changed
  LIKE(1),
  LOVE(2),
  HAPPY(3),
  WOW(4),
  SAD(5),
  ANGRY(6);

  public int id;

  LikeType(int id) {
    this.id = id;
  }

  public static LikeType fromName(String name) {
    for (LikeType likeType : LikeType.values()) {
      if (likeType.name().equalsIgnoreCase(name)) {
        return likeType;
      }
    }
    return null;
  }
}