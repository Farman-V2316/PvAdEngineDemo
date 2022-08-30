/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides utility functions to handle data.
 *
 * @author shreyas.desai
 */
public class DataUtil {

  /**
   * Parses string to float. Returns float value or default value in case of error.
   *
   * @param floatStr   value to be parsed
   * @param defaultVal value to be returned in case of error
   * @return parsed value of floatStr or defaultVal in case of error
   */
  public static float parseFloat(String floatStr, float defaultVal) {
    if (floatStr == null) {
      return defaultVal;
    }

    try {
      return Float.parseFloat(floatStr);
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }

  /**
   * Parses string to int. Returns int value or default value in case of error.
   *
   * @param intStr     value to be parsed
   * @param defaultVal value to be returned in case of error
   * @return parsed value of intStr or defaultVal in case of error
   */
  public static int parseInt(String intStr, int defaultVal) {
    if (isEmpty(intStr)) {
      return defaultVal;
    }
    try {
      return Integer.parseInt(intStr);
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }

  /**
   * Parses string to long. Returns long value or default value in case of error.
   *
   * @param longStr    value to be parsed
   * @param defaultVal value to be returned in case of error
   * @return parsed value of longStr or defaultVal in case of error
   */
  public static long parseLong(String longStr, long defaultVal) {
    if (longStr == null) {
      return defaultVal;
    }
    try {
      return Long.parseLong(longStr);
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }

  /**
   * Parses string to double. Returns int value or default value in case of error.
   *
   * @param doubleStr  value to be parsed
   * @param defaultVal value to be returned in case of error
   * @return parsed value of intStr or defaultVal in case of error
   */
  public static double parseDouble(String doubleStr, double defaultVal) {
    if (isEmpty(doubleStr)) {
      return defaultVal;
    }

    try {
      return Double.parseDouble(doubleStr);
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }

  public static boolean equalsIgnoreCase(String first, String second) {
    if (first == null && second == null) {
      return true;
    }

    if (first == null || second == null) {
      return false;
    }

    return first.equalsIgnoreCase(second);

  }

  public static boolean isEmpty(String str) {
    return str == null || str.trim().length() == 0;
  }

  public static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }

  public static String parseAsString(Collection<String> strings) {
    if (strings == null || strings.size() == 0) {
      return "";
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (String str : strings) {
      if (stringBuilder.length() > 0) {
        stringBuilder.append(",");
      }

      stringBuilder.append(str);
    }

    return stringBuilder.toString();
  }

  public static String parseAsString(Collection<String> strings, String delimiter) {
    if (strings == null || strings.size() == 0) {
      return "";
    }

    StringBuilder stringBuilder = new StringBuilder();
    for (String str : strings) {
      if (stringBuilder.length() > 0) {
        stringBuilder.append(delimiter);
      }

      stringBuilder.append(str);
    }

    return stringBuilder.toString();
  }

  public static List<String> parsAsList(String stringAsList, String delimiter) {
    List<String> strings = new ArrayList<>();
    if (isEmpty(stringAsList)) {
      return strings;
    }

    StringTokenizer stringTokenizer = new StringTokenizer(stringAsList, delimiter);
    while (stringTokenizer.hasMoreTokens()) {
      String key = stringTokenizer.nextToken();
      strings.add(key);
    }

    return strings;
  }

  public static Boolean parseBoolean(String booleanStr, Boolean defaultVal) {
    if (isEmpty(booleanStr)) {
      return defaultVal;
    }

    try {
      return Boolean.parseBoolean(booleanStr);
    } catch (NumberFormatException e) {
      return defaultVal;
    }
  }

  /**
   * Html.fromHtml() appends "Object-replacement-character(\uFFFC)". It will be visible when
   * Spanned is converted to string and shown in textview. Helper method to remove that character.
   */
  public static String removeObjReplacementChar(String s) {
    if (!isEmpty(s)) {
      return s.replaceAll("\uFFFC", Constants.EMPTY_STRING);
    }
    return s;
  }

  /**
   * Convert number to Easy readable string
   */
  public static String easyReadableString(long count) {
    if (count < 1000) return Long.toString(count);
    int exp = (int) (Math.log(count) / Math.log(1000));
    return String.format("%d%c", (int) (count / Math.pow(1000, exp)), "kMGTPE".charAt(exp-1));
  }
}
