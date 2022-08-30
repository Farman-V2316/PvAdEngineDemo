/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.ioutils;

/**
 * provides IO utility functions
 *
 * @author santosh.kulkarni
 */

public class IOUtilsExtraHelper {
  public static String SYMMETRIC_ALGORITHM = "AES";
  public static String SYMMETRIC_ALGORITHM_MODE = "CBC";
  public static String SYMMETRIC_ALGORITHM_PADDING = "PKCS5Padding";
  public static String SYMMETRIC_CIPHER = SYMMETRIC_ALGORITHM + "/"
      + SYMMETRIC_ALGORITHM_MODE + "/" + SYMMETRIC_ALGORITHM_PADDING;
}
