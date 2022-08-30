/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.common.helper.cachedapi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author arun.babu
 */
public class CacheCompressUtils {

  public static byte[] compress(String data) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
    GZIPOutputStream gzip = new GZIPOutputStream(bos);
    gzip.write(data.getBytes());
    gzip.close();
    byte[] compressed = bos.toByteArray();
    bos.close();
    return compressed;
  }

  public static byte[] compress(byte[] data) throws IOException {
    ByteArrayInputStream ins = new ByteArrayInputStream(data);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    GZIPOutputStream gzip = new GZIPOutputStream(bos);
    byte[] buffer = new byte[1024];
    int len;
    while ((len = ins.read(buffer)) != -1) {
      gzip.write(buffer, 0, len);
    }
    gzip.close();
    ins.close();
    bos.close();
    return bos.toByteArray();
  }

  public static byte[] decompressToByteArray(byte[] compressed) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
    GZIPInputStream gis = new GZIPInputStream(bis);
    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    byte[] buffer = new byte[1024];
    int len;
    while ((len = gis.read(buffer)) != -1) {
      bos.write(buffer, 0, len);
    }
    bos.close();
    gis.close();
    bis.close();
    return bos.toByteArray();
  }

  public static String decompress(byte[] compressed) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
    GZIPInputStream gis = new GZIPInputStream(bis);
    BufferedReader br = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    gis.close();
    bis.close();
    return sb.toString();
  }
}
