/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */
package com.newshunt.common.helper.info;

import android.text.TextUtils;
import android.util.Base64;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientIdHelper {

  private static final String TAG = "ClientIdHelper";
  private static final String CODES =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  private static final char CODEFLAG = '9';

  private static StringBuilder out = new StringBuilder();

  private static Map<Character, Integer> CODEMAP = new HashMap<Character, Integer>();

  private static void Append(int b) {
    if (b < 61) {
      out.append(CODES.charAt(b));
    } else {

      out.append(CODEFLAG);

      out.append(CODES.charAt(b - 61));
    }
  }

  private static String base62Encode(byte[] in) {

    // Reset output StringBuilder
    out.setLength(0);

    //
    int b;

    // Loop with 3 bytes as a group
    for (int i = 0; i < in.length; i += 3) {

      // #1 char
      b = (in[i] & 0xFC) >> 2;
      Append(b);

      b = (in[i] & 0x03) << 4;
      if (i + 1 < in.length) {

        // #2 char
        b |= (in[i + 1] & 0xF0) >> 4;
        Append(b);

        b = (in[i + 1] & 0x0F) << 2;
        if (i + 2 < in.length) {

          // #3 char
          b |= (in[i + 2] & 0xC0) >> 6;
          Append(b);

          // #4 char
          b = in[i + 2] & 0x3F;
          Append(b);

        } else {
          // #3 char, last char
          Append(b);
        }
      } else {
        // #2 char, last char
        Append(b);
      }
    }

    return out.toString();
  }

  public static byte[] base62Decode(char[] inChars) {

    // Map for special code followed by CODEFLAG '9' and its code index
    CODEMAP.put('A', 61);
    CODEMAP.put('B', 62);
    CODEMAP.put('C', 63);

    ArrayList<Byte> decodedList = new ArrayList<Byte>();

    // 6 bits bytes
    int[] unit = new int[4];

    int inputLen = inChars.length;

    // char counter
    int n = 0;

    // unit counter
    int m = 0;

    // regular char
    char ch1 = 0;

    // special char
    char ch2 = 0;

    Byte b = 0;

    while (n < inputLen) {
      ch1 = inChars[n];
      if (ch1 != CODEFLAG) {
        // regular code
        unit[m] = CODES.indexOf(ch1);
        m++;
        n++;
      } else {
        n++;
        if (n < inputLen) {
          ch2 = inChars[n];
          if (ch2 != CODEFLAG) {
            // special code index 61, 62, 63, 64
            unit[m] = CODEMAP.get(ch2);
            m++;
            n++;
          }
        }
      }

      // Add regular bytes with 3 bytes group composed from 4 units with 6 bits.
      if (m == 4) {
        b = new Byte((byte) ((unit[0] << 2) | (unit[1] >> 4)));
        decodedList.add(b);
        b = new Byte((byte) ((unit[1] << 4) | (unit[2] >> 2)));
        decodedList.add(b);
        b = new Byte((byte) ((unit[2] << 6) | unit[3]));
        decodedList.add(b);

        // Reset unit counter
        m = 0;
      }
    }

    // Add tail bytes group less than 4 units
    if (m != 0) {
      if (m == 1) {
        b = new Byte((byte) ((unit[0] << 2)));
        decodedList.add(b);
      } else if (m == 2) {
        b = new Byte((byte) ((unit[0] << 2) | (unit[1] >> 4)));
        decodedList.add(b);
      } else if (m == 3) {
        b = new Byte((byte) ((unit[0] << 2) | (unit[1] >> 4)));
        decodedList.add(b);
        b = new Byte((byte) ((unit[1] << 4) | (unit[2] >> 2)));
        decodedList.add(b);
      }
    }

    Byte[] decodedObj = decodedList.toArray(new Byte[decodedList.size()]);

    byte[] decoded = new byte[decodedObj.length];

    // Convert object Byte array to primitive byte array
    for (int i = 0; i < decodedObj.length; i++) {
      decoded[i] = (byte) decodedObj[i];

    }

    return decoded;
  }

  private static synchronized String encrypt(String plaintext,
                                             String algorithm, String encoding) throws Exception {
    MessageDigest msgDigest = null;
    String hashValue = null;
    try {
      msgDigest = MessageDigest.getInstance(algorithm);
      msgDigest.update(plaintext.getBytes(encoding));
      byte[] rawByte = msgDigest.digest();

      hashValue = Base64.encodeToString(rawByte, Base64.NO_WRAP);

    } catch (NoSuchAlgorithmException e) {
      System.out.println("No Such Algorithm Exists");
    } catch (UnsupportedEncodingException e) {
      System.out.println("The Encoding Is Not Supported");
    }
    return hashValue;
  }

  private static String generateClientId(String androidId) {
    Logger.e(TAG, "Generate Client Generated Client id for: " + androidId);
    try {
      return base62Encode(encrypt(androidId, "MD5", "UTF-8").getBytes());
    } catch (Exception e) {
      Logger.e(TAG, "exception generating client Generated Client id");
      return "";
    }
  }

  public static void generateAndSaveClientId() {
    Logger.d(TAG, "generateAndSaveClientGeneratedClientId");
    String clientId = ClientInfoHelper.getClientGeneratedClientId();
    if (TextUtils.isEmpty(clientId)) {
      String newClientId = generateClientId(ClientInfoHelper.getAndroidId());
      Logger.d(TAG, "Client Generated Client id: " + newClientId);
      UserPreferenceUtil.saveClientGeneratedClientId("dh." + newClientId);
    }
  }

}
