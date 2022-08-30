/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * An Utility class for to/from Json conversion
 *
 * @author santhosh.kc
 */
public class JsonUtils {

  /**
   * Utility function to get JsonObject from string representation of Json object
   *
   * @param jsonString - string representation
   * @return - converted JsonObject, on any invalid string input, this function will return null
   */
  public static JsonObject fromJson(String jsonString) {
    return fromJson(jsonString, JsonObject.class);
  }

  /**
   * Utility function to get Object of class of T from json string representation of the object
   *
   * @param jsonString   - string representation
   * @param classOfT     - class of T
   * @param typeAdapters - type adapters for any member variables
   * @param <T>          - generic representation of object
   * @return - object whose class is of type T
   */
  public static <T> T fromJson(String jsonString, Class<T> classOfT,
                               NHJsonTypeAdapter... typeAdapters) {
    if (CommonUtils.isEmpty(jsonString)) {
      return null;
    }
    GsonBuilder builder = new GsonBuilder();
    for (NHJsonTypeAdapter deserializer : typeAdapters) {
      builder.registerTypeAdapter(deserializer.getTypeOfT(), deserializer);
    }

    Gson gson = builder.create();
    T value = null;
    try {
      value = gson.fromJson(jsonString, classOfT);
    } catch (JsonSyntaxException e) {
      Logger.caughtException(e);
    }
    return value;
  }

  /**
   * Utility function to get Object of type T from json string representation of the object
   *
   * @param jsonString   - string representation
   * @param typeOfT      - type of T
   * @param typeAdapters - type adapters for any member variables
   * @param <T>          - generic representation of object
   * @return - object of type T
   */
  @Nullable
  public static <T> T fromJson(String jsonString, Type typeOfT,
                               NHJsonTypeAdapter... typeAdapters) {
    if (CommonUtils.isEmpty(jsonString)) {
      return null;
    }
    try {
      return fromJsonOrThrow(jsonString, typeOfT, typeAdapters);
    } catch (JsonSyntaxException e) {
      Logger.caughtException(e);
      return null;
    }
  }

  @NonNull
  public static <T> T fromJsonOrThrow(@NonNull String jsonString, Type typeOfT,
                               NHJsonTypeAdapter... typeAdapters) throws JsonSyntaxException {
    GsonBuilder builder = new GsonBuilder();
    for (NHJsonTypeAdapter deserializer : typeAdapters) {
      if (deserializer == null) {
        continue;
      }
      builder.registerTypeAdapter(deserializer.getTypeOfT(), deserializer);
    }
    Gson gson = builder.create();
    return gson.fromJson(jsonString, typeOfT);
  }

  /**
   * Utility function to get Object of type T from json string representation of the object
   *
   * @param jsonElement  - jsonElement
   * @param typeOfT      - type of T
   * @param typeAdapters - type adapters for any member variables
   * @param <T>          - generic representation of object
   * @return - object of type T
   */
  public static <T> T fromJson(JsonElement jsonElement, Type typeOfT,
                               NHJsonTypeAdapter... typeAdapters) {
    if (jsonElement == null) {
      return null;
    }

    GsonBuilder builder = new GsonBuilder();
    for (NHJsonTypeAdapter deserializer : typeAdapters) {
      builder.registerTypeAdapter(deserializer.getTypeOfT(), deserializer);
    }

    Gson gson = builder.create();
    T value = null;
    try {
      value = gson.fromJson(jsonElement, typeOfT);
    } catch (JsonSyntaxException e) {
      Logger.caughtException(e);
    }
    return value;
  }

  /**
   * Utility function to get json string representation of object of type T from json string
   *
   * @param object - object of type T
   * @param <T>    - generic T
   * @return - Json string representation of the object of type T
   */
  public static <T> String toJson(T object) {
    if (object == null) {
      return Constants.EMPTY_STRING;
    }

    Gson gson = new Gson();
    String jsonString = Constants.EMPTY_STRING;
    try {
      // getType on generic is not used because it doesnt work correctly in some cases on
      // Android 7.0
      // More: http://stackoverflow.com/questions/5370768/using-a-generic-type-with-gson
      jsonString = gson.toJson(object, object.getClass());
    } catch (JsonSyntaxException e) {
      Logger.caughtException(e);
    }
    return jsonString;
  }

  public static String getJsonString(Bundle bundle) {
    if (bundle == null) {
      return Constants.EMPTY_STRING;
    }
    JSONObject jsonObject = new JSONObject();
    for (String key : bundle.keySet()) {
      try {
        jsonObject.put(key, bundle.get(key).toString());
      } catch (Exception e) {
        // Do nothing.
      }
    }
    return jsonObject.toString();
  }

}