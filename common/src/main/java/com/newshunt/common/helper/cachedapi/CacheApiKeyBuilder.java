/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.common.helper.cachedapi;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okio.ByteString;

/**
 * @author arun.babu
 */
public class CacheApiKeyBuilder {

  private final StringBuffer keyBuffer = new StringBuffer();

  public void addParams(String... keyVals) throws ArrayIndexOutOfBoundsException{
    for (int i = 0; i < keyVals.length; i = i + 2) {
      addParam(keyVals[i], keyVals[i + 1]);
    }
  }

  public void addParam(String param, String value) {
    if (param != null) {
      keyBuffer.append(param.replaceAll("[^\\w\\s]","").toLowerCase().trim());
    }

    keyBuffer.append("_");

    if (param != null) {
      keyBuffer.append(value.replaceAll("[^\\w\\s]","").toLowerCase().trim());
    }

    keyBuffer.append("_");
  }

  public String build() {
    return md5Hex(keyBuffer.toString());
  }

  /** Returns a 32 character string containing an MD5 hash of {@code s}. */
  public static String md5Hex(String s) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      byte[] md5bytes = messageDigest.digest(s.getBytes("UTF-8"));
      return ByteString.of(md5bytes).hex();
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      throw new AssertionError(e);
    }
  }
}
