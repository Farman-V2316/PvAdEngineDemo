/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.sso.helper;


import android.util.Base64;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Hash generator shared by B.E Team to hash the client ID.
 * Please do not change anything in this file without consulting Gangadhar Swamy
 * <p>
 * Created by srikanth.ramaswamy on 07/10/2019.
 */
public class CustomHashGenerator {
  private static final String SHA_266_ENCRYPT = "SHA-256";
  private static final String DEFAULT_ENCODING = "UTF-8";

  public static String getHash(String value) {
    String hash = null;

    if (!CommonUtils.isEmpty(value)) {
      String valueTrim = value.trim();
      String jumbledA = getJumbleA(valueTrim);
      String jumbledB = getJumbleB(valueTrim);
      String userName = encryptWithSalt(jumbledA, jumbledB, 13);

      hash = userName.substring(0, userName.length() - 2);
    }

    return hash;
  }

  private static String getJumbleA(String value) {
    int length = value.length();
    char[] doubleChars = new char[length * 2];

    for (int i = 0; i < length; i++) {
      int doubleCharsIndex = i * 2;

      doubleChars[doubleCharsIndex] = (char) (((int) value.charAt(i) * i) % length);
      doubleChars[doubleCharsIndex + 1] =
          (char) (((int) value.charAt(i) * doubleCharsIndex) >>> length);
    }

    return new String(doubleChars);
  }

  private static String getJumbleB(String value) {
    char[] chars = value.toCharArray();
    char[] doubleChars = new char[chars.length * 2];

    int lastIndex = chars.length - 1;
    for (int i = 0; i < chars.length; i++) {
      int doubleCharsIndex = i * 2;

      doubleChars[doubleCharsIndex] =
          (char) (((int) value.charAt(lastIndex - i) * i) >>> lastIndex);
      doubleChars[doubleCharsIndex + 1] =
          (char) (((int) value.charAt(i) * doubleCharsIndex) / lastIndex);
    }

    return new String(doubleChars);
  }

  private static String encryptWithSalt(String word, String salt, int harden) {
    String hash = null;


    try {
      MessageDigest messageDigest = MessageDigest.getInstance(SHA_266_ENCRYPT);
      messageDigest.reset();
      messageDigest.update(salt.getBytes(DEFAULT_ENCODING));
      byte[] raw = messageDigest.digest(word.getBytes(DEFAULT_ENCODING));

      for (int i = 0; i < harden; i++) {
        messageDigest.reset();
        raw = messageDigest.digest(raw);
      }

      hash = Base64.encodeToString(raw, Base64.NO_WRAP);
    } catch (NoSuchAlgorithmException e) {
      Logger.caughtException(e);
    } catch (UnsupportedEncodingException e) {
      Logger.caughtException(e);
    }

    return hash;
  }
}