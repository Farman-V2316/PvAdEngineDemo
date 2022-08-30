/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.newshunt.adengine.model.entity.version.AdContentType;
import com.newshunt.common.helper.common.NHJsonTypeAdapter;

import java.lang.reflect.Type;


/**
 * Created by {mukesh.yadav} on 12,November,2019
 */
public class AdTypeDeserializer extends NHJsonTypeAdapter<AdContentType> {
  public AdTypeDeserializer(Type typeOf) {
    super(typeOf);
  }

  @Override
  public AdContentType deserialize(
      JsonElement jsonElement,
      Type type,
      JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

    String assetType = jsonElement.getAsString();
    return AdContentType.fromName(assetType);
  }
}
