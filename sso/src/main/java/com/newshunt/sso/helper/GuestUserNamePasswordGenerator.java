/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.helper;

import android.util.Base64;

import com.newshunt.sso.model.entity.Credential;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Guest login username and password generator
 *
 * @author arun.babu
 */
public class GuestUserNamePasswordGenerator {
  private static final String SHA_266_ENCRYPT = "SHA-256";

  public static Credential getCredentials(String udid) {
    String udidTrim = udid.trim();
    String jumbledA = getJumbleA(udidTrim);
    String jumbledB = getJumbleB(udidTrim);
    String userName = encryptWithSalt(jumbledA, jumbledB, 13);
    String password = encryptWithSalt(jumbledB, jumbledA, 17);

    return new Credential(userName.substring(0, userName.length() - 2),
        password.substring(0, userName.length() - 2));
  }

  private static String getJumbleA(String udid) {
    int length = udid.length();
    char[] doubleChars = new char[length * 2];

    for (int i = 0; i < length; i++) {
      int doubleCharsIndex = i * 2;
      doubleChars[doubleCharsIndex] = (char) (((int) udid.charAt(i) * i) % length);
      doubleChars[doubleCharsIndex + 1] = (char) (((int) udid.charAt(i) * doubleCharsIndex) >>>
          length);
    }

    return new String(doubleChars);
  }

  private static String getJumbleB(String udid) {
    char[] chars = udid.toCharArray();
    char[] doubleChars = new char[chars.length * 2];

    int lastIndex = chars.length - 1;
    for (int i = 0; i < chars.length; i++) {
      int doubleCharsIndex = i * 2;
      doubleChars[doubleCharsIndex] = (char) (((int) udid.charAt(lastIndex - i) * i) >>> lastIndex);
      doubleChars[doubleCharsIndex + 1] = (char) (((int) udid.charAt(i) * doubleCharsIndex) /
          lastIndex);
    }

    return new String(doubleChars);
  }

  private static String encryptWithSalt(String word, String salt, int harden) {
    String hash = null;
    try {
      MessageDigest messageDigest = MessageDigest.getInstance(SHA_266_ENCRYPT);
      messageDigest.reset();
      messageDigest.update(salt.getBytes(Charset.forName("UTF-8")));
      byte[] raw = messageDigest.digest(word.getBytes(Charset.forName("UTF-8")));

      for (int i = 0; i < harden; i++) {
        messageDigest.reset();
        raw = messageDigest.digest(raw);
      }

      hash = Base64.encodeToString(raw, Base64.NO_WRAP);
    } catch (Exception e) {
      //e.printStackTrace();
    }
    return hash;
  }
}
