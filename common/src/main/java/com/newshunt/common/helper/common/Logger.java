/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.util.Log;

import com.newshunt.common.helper.LogCollectionUtils;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.internal.NetworkSDKLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Shall be used for all logging requirements in the source code. Global flag in this file can be
 * used to disable logging when going for production.
 *
 * @author arun.babu
 */
public class Logger {
  //PANDA:
 /* private static boolean loggerEnabled = AppConfig.getInstance() != null && AppConfig.getInstance()
      .isLoggerEnabled();
*/

  private static boolean loggerEnabled = true;

  private static boolean enableFileLogging = false;

  private static FileWriter logFileWriter;
  private static File currentFile;
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault());

  public static boolean loggerEnabled() {
    return loggerEnabled;
  }

  public static void setFileLogger(boolean enable) {
    enableFileLogging = enable;
    if (enable) {
      currentFile = LogCollectionUtils.getWriteableFile(null);
      try {
        logFileWriter = new FileWriter(currentFile, true);
        logFileWriter.write("New File for " + ClientInfoHelper.getClientId() + "\n");
      } catch (Exception ex) {
        Log.e("Logger", "Unable to write to log file", ex);
      }
    }
  }

  public static void setFileLogger(boolean enableFileLogging, File logFile) {
    if (enableFileLogging && logFileWriter == null && logFile != null) {
      try {
        logFileWriter = new FileWriter(logFile, true);
        currentFile = logFile;
      } catch (IOException e) {
        Log.e("Logger", "Unable to write to log file", e);
      }
    }

    if (!enableFileLogging && logFileWriter != null) {
      try {
        logFileWriter.flush();
        logFileWriter.close();
      } catch (IOException e) {
        Log.e("Logger", "Error flushing log contents");
      }
    }

    if (Logger.loggerEnabled()) Logger.enableFileLogging = enableFileLogging;
  }

  public static void setLoggerEnabled(boolean enabled) {
    loggerEnabled = enabled;
    NetworkSDK.setLogEnabled(enabled);
  }

  public static void d(String aTag, String aMessage) {
    if (loggerEnabled) {
      Log.d(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage);
      writeToFile(aTag, aMessage, null, "d");
    }
  }

  private static synchronized void writeToFile(String aTag, String aMessage, Throwable e,
                                               String severity) {
    if (enableFileLogging && logFileWriter != null) {
      try {
        Date date = new Date();
        logFileWriter.write(dateFormat.format(date));
        logFileWriter.write(Constants.SPACE_STRING);
        logFileWriter.write(aTag);
        logFileWriter.write(Constants.SPACE_STRING);
        logFileWriter.write(severity);
        logFileWriter.write(Constants.SPACE_STRING);
        logFileWriter.write(aMessage);
        logFileWriter.write("\n");
        if (e != null) {
          PrintWriter pw = new PrintWriter(logFileWriter, true);
          e.printStackTrace(pw);
          pw.flush();
          logFileWriter.write("\n");
        }
        logFileWriter.flush();

        if (currentFile.length() > Constants.TWO_MB_IN_BYTES) {
          // Get new file
          currentFile = LogCollectionUtils.getNextLogFile();
          try {
            logFileWriter = new FileWriter(currentFile, true);
            logFileWriter.write("New File for " + ClientInfoHelper.getClientId() + "\n");
          } catch (Exception ex) {
            Log.e("Logger", "Unable to write to log file", ex);
          }
        }

        Log.e("Logger", "File:" + currentFile.getName() + " : " + currentFile.length());
      } catch (IOException ex) {
        Log.e("Logger", "Error writing to file", ex);
      }
    }
  }

  public static void d(String aTag, String aMessage, Throwable e) {
    if (loggerEnabled) {
      Log.d(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage, e);
      writeToFile(aTag, aMessage, e, "d");
    }
  }


  public static void w(String aTag, String aMessage) {
    if (loggerEnabled) {
      Log.w(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage);
      writeToFile(aTag, aMessage, null, "w");
    }
  }

  public static void e(String aTag, String aMessage) {
    if (loggerEnabled) {
      Log.e(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage);
      writeToFile(aTag, aMessage, null, "e");
    }
  }

  public static void i(String aTag, String aMessage) {
    if (loggerEnabled) {
      Log.i(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage);
      writeToFile(aTag, aMessage, null, "i");
    }
  }

  public static void e(String aTag, String aMessage, Throwable e) {
    if (loggerEnabled) {
      Log.e(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage, e);
      writeToFile(aTag, aMessage, e, "e");
    }
  }

  public static void w(String aTag, String aMessage, Exception e) {
    if (loggerEnabled) {
      Log.w(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage, e);
      writeToFile(aTag, aMessage, e, "w");
    }
  }

  public static void v(String aTag, String aMessage) {
    if (loggerEnabled) {
      Log.v(aTag, aMessage == null ? Constants.EMPTY_STRING + null : aMessage);
      writeToFile(aTag, aMessage, null, "v");
    }
  }

  public static void caughtException(Throwable e) {
    if (!loggerEnabled || null == e) {
      return;
    }

    if (e.getMessage() != null) {
      Log.e("Caught Exception", e.getMessage());
      writeToFile("Caught Exception", e.getMessage(), e, "e");
    } else {
      Log.e("Caught Exception", e.toString());
      writeToFile("Caught Exception", e.toString(), e, "e");
    }
  }

  /**
   * Network SDK Logger instance to pass network SDK logs to custom Logger APIs
   */
  public static NetworkSDKLogger.Logger getLogger() {
    return new NetworkSDKLogger.Logger() {
      @Override
      public int d(String s, String s1) {
        if (Logger.loggerEnabled()) Logger.d(s, s1);
        return 0;
      }

      @Override
      public int d(String s, String s1, Throwable throwable) {
        if (Logger.loggerEnabled()) Logger.d(s, s1, throwable);
        return 0;
      }

      @Override
      public int w(String s, String s1) {
        if (Logger.loggerEnabled()) Logger.w(s, s1);
        return 0;
      }

      @Override
      public int e(String s, String s1) {
        if (Logger.loggerEnabled()) Logger.e(s, s1);
        return 0;
      }

      @Override
      public int i(String s, String s1) {
        if (Logger.loggerEnabled()) Logger.i(s, s1);
        return 0;
      }

      @Override
      public int e(String s, String s1, Exception e) {
        if (Logger.loggerEnabled()) Logger.e(s, s1, e);
        return 0;
      }

      @Override
      public int w(String s, String s1, Exception e) {
        if (Logger.loggerEnabled()) Logger.w(s, s1, e);
        return 0;
      }

      @Override
      public int v(String s, String s1) {
        if (Logger.loggerEnabled()) Logger.v(s, s1);
        return 0;
      }
    };
  }
}
