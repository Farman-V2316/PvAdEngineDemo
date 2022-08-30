/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * An abstract class to convert member variables of Type T from Json representation of the member
 * variable. The child class must implement deserialize method
 *
 * @author santhosh.kc
 */
public class NHJsonTypeAdapter<T> implements JsonDeserializer<T> {

  private final Type typeOfT;

  public NHJsonTypeAdapter(Type typeOf) {
    this.typeOfT = typeOf;
  }

  public Type getTypeOfT() {
    return typeOfT;
  }

  @Override
  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    T pojo = new Gson().fromJson(json, typeOfT);
    return pojo;
  }
}
