/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;

import java.util.Set;

/**
 * Common class for saving data in preference
 *
 * @author datta.vitore.
 */
public class PreferenceManager {

  private static PreferenceDao preferenceDao = new PreferenceDao();
  private static String TAG = "PreferenceManager";

  public static boolean containsPreference(SavedPreference savedPreference) {
    PreferenceType preferenceType = savedPreference.getPreferenceType();
    if (preferenceType == null) {
      return false;
    }
    Context context = CommonUtils.getApplication();
    String preferenceFileName = preferenceType.getFileName();
    String preferenceKey = savedPreference.getName();
    if (AppConfig.getInstance().isGoBuild()) {
      return preferenceDao.contains(preferenceFileName, preferenceKey);
    }
    else {
      SharedPreferences sharedPreferences =
          context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);

      return sharedPreferences.contains(preferenceKey);
    }
  }

  public static void savePreference(SavedPreference savedPreference, Object value) {
    savePreference(savedPreference.getPreferenceType(), savedPreference.getName(), value);
  }

  public static void savePreference(Context context, SavedPreference savedPreference,
                                    Object value, Context alternativeContext) {
    savePreference(context, savedPreference.getPreferenceType(), savedPreference.getName(), value, alternativeContext);
  }


  private static void savePreference(PreferenceType preferenceType, String key, Object value) {

    Context context = CommonUtils.getApplication();
    savePreference(context, preferenceType, key, value, null);
  }

  public static void savePreference(Context context, PreferenceType preferenceType, String key,
                                    Object value, Context alternativeContext) {
    savePreference(context, preferenceType, key, value, false, alternativeContext);
  }

  public static void savePreference(Context context, PreferenceType preferenceType, String key,
                                    Object value, boolean forceRunOnMainProcess, Context alternativeContext) {
    if (context == null) {
      return;
    }
    String fileName = PreferenceType.APP_STATE.getFileName();
    if (preferenceType != null) {
      fileName = preferenceType.getFileName();
    }

    // If forceRunOnMainProcess is set to true, then don't get preference value through content
    // provider path.
    if (!forceRunOnMainProcess && !CommonUtils.isMainProcess()) {
      Logger.d(TAG, "savePreference called from outside of Main Process");
      PreferenceDataType dataType = PreferenceDataType.getDataType(value);
      String file = preferenceType == null ? fileName : preferenceType.getFileName();
      new MultiProcessPreferenceManager().savePreference(file, dataType, key, value, alternativeContext);
      return;
    }

    if (value instanceof String) {
      if (AppConfig.getInstance().isGoBuild()) {
        preferenceDao.savePreference(fileName, key, value.toString());
      }
      else {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
            fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value.toString());
        editor.apply();
      }
    } else if (value instanceof Integer) {
      if (AppConfig.getInstance().isGoBuild()) {
        preferenceDao.savePreference(fileName, key, value.toString());
      }
      else {
        Integer intValue = (Integer) value;
        if (intValue != -1) {
          SharedPreferences sharedPreferences = context.getSharedPreferences(
              fileName, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putInt(key, intValue);
          editor.apply();
        }
      }
    } else if (value instanceof Long) {
      if (AppConfig.getInstance().isGoBuild()) {
        preferenceDao.savePreference(fileName, key, value.toString());
      }
      else {
        Long longValue = (Long) value;
        if (longValue != -1) {
          SharedPreferences sharedPreferences = context.getSharedPreferences(
              fileName, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putLong(key, longValue);
          editor.apply();
        }
      }
    } else if (value instanceof Boolean) {
      if (AppConfig.getInstance().isGoBuild()) {
        preferenceDao.savePreference(fileName, key, value.toString());
      }
      else {
        Boolean booleanValue = (Boolean) value;
        if (booleanValue != null) {
          SharedPreferences sharedPreferences = context.getSharedPreferences(
              fileName, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putBoolean(key, booleanValue);
          editor.apply();
        }
      }
    } else if (value instanceof Set) {
      Set<String> stringSet = (Set<String>) value;
      SharedPreferences sharedPreferences = context.getSharedPreferences(
          fileName, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putStringSet(key, stringSet);
      editor.apply();
    } else if (value instanceof Float) {
      if (AppConfig.getInstance().isGoBuild()) {
        preferenceDao.savePreference(fileName, key, value.toString());
      }
      else {
        Float floatValue = (Float) value;
        if (Float.compare(floatValue, -1.0f) != 0) {
          SharedPreferences sharedPreferences = context.getSharedPreferences(
              fileName, Context.MODE_PRIVATE);
          SharedPreferences.Editor editor = sharedPreferences.edit();
          editor.putFloat(key, floatValue);
          editor.apply();
        }
      }
    }
  }

  /**
   * reads preference value and saves only when new value is different from old value
   */
  public static boolean savePreferenceIfChanged(SavedPreference savedPreference, Object value) {
    if (value == null) {
      return false;
    }

    if (value instanceof String) {
      String savedPref = getPreference(savedPreference, Constants.EMPTY_STRING);
      if (!savedPref.equals(Constants.EMPTY_STRING) && savedPref.equals(value.toString())) {
        // already value is saved. no need to edit
      } else {
        savePreference(savedPreference.getPreferenceType(), savedPreference.getName(), value);
      }
    } else if (value instanceof Integer) {
      int savedPref = getPreference(savedPreference, -1);
      if (savedPref != -1 && savedPref == ((Integer) value).intValue()) {
        // already value is saved. no need to edit
      } else {
        savePreference(savedPreference.getPreferenceType(), savedPreference.getName(), value);
      }
    } else if (value instanceof Long) {
      Long savedPref = getPreference(savedPreference, -1L);
      if (savedPref != -1 && savedPref == ((Long) value).longValue()) {
        // already value is saved. no need to edit
      } else {
        savePreference(savedPreference.getPreferenceType(), savedPreference.getName(), value);
      }
    } else if (value instanceof Boolean) {
      Boolean savedPref = getPreference(savedPreference, false);
      if (savedPref != value) {
        savePreference(savedPreference.getPreferenceType(), savedPreference.getName(), value);
      }
    }

    return true;
  }

  public static void saveStringSet(String key, Set<String> value) {
    savePreference(null, key, value);
  }

  public static void saveString(String key, String value) {
    savePreference(null, key, value);
  }

  public static void saveInt(String key, int value) {
    savePreference(null, key, value);
  }

  public static void saveLong(String key, long value) {
    savePreference(null, key, value);
  }

  public static void saveFloat(String key, float value) {
    savePreference(null, key, value);
  }

  public static void saveLong(SavedPreference savedPreference, long value) {
    savePreference(savedPreference.getPreferenceType(), savedPreference.getName(), value);
  }

  public static void saveBoolean(String key, boolean value) {
    savePreference(null, key, value);
  }

  public static <T> T getPreference(SavedPreference savedPreference, T defaultVal) {
    return getPreference(savedPreference.getPreferenceType(), savedPreference.getName(),
        defaultVal);
  }

  public static <T> T getPreference(Context context, SavedPreference savedPreference, T
      defaultVal) {
    return getPreference(context, savedPreference.getPreferenceType(), savedPreference.getName(),
        defaultVal);
  }

  public static <T> T getPreference(PreferenceType preferenceType, String key, T defaultVal) {
    Context context = CommonUtils.getApplication();
    return getPreference(context, preferenceType, key, defaultVal);
  }

  public static <T> T getPreference(Context context, PreferenceType preferenceType, String key, T
      defaultVal) {
    return getPreference(context, preferenceType, key, defaultVal, false);
  }

  public static <T> T getPreference(Context context, PreferenceType preferenceType, String key, T
      defaultVal, boolean forceRunOnMainProcess) {

    try {
      if (context == null) {
        return defaultVal;
      }
      String fileName = PreferenceType.APP_STATE.getFileName();
      if (preferenceType != null) {
        fileName = preferenceType.getFileName();
      }

      //If forceRunOnMainProcess is true, then don't run on try to get the value through content
      // provider.
      if (!forceRunOnMainProcess && !CommonUtils.isMainProcess()) {
        Logger.d(TAG, "getPreference called from outside of Main Process");
        PreferenceDataType dataType = PreferenceDataType.getDataType(defaultVal);
        String file = preferenceType == null ? fileName : preferenceType.getFileName();
        return new MultiProcessPreferenceManager().getPreference(file, dataType, key, defaultVal);
      }

      if (defaultVal instanceof String) {
        if (AppConfig.getInstance().isGoBuild()) {
          String value = preferenceDao.getPreference(fileName, key);
          if (value == null) {
            value = (String) defaultVal;
          }
          return (T) value;
        }
        else {
          SharedPreferences sharedPreferences =
              context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
          return (T) sharedPreferences.getString(key, defaultVal.toString());
        }
      } else if (defaultVal instanceof Integer) {
        if (AppConfig.getInstance().isGoBuild()) {
          String strValue = preferenceDao.getPreference(fileName, key);
          if (strValue == null) {
            strValue = Integer.toString((Integer) defaultVal);
          }
          return (T) new Integer(Integer.parseInt(strValue));
        }
        else {
          SharedPreferences sharedPreferences =
              context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
          Integer defIntValue = (Integer) defaultVal;
          Integer intValue = sharedPreferences.getInt(key, defIntValue);
          return (T) intValue;
        }
      } else if (defaultVal instanceof Long) {
        if (AppConfig.getInstance().isGoBuild()) {
          String strValue = preferenceDao.getPreference(fileName, key);
          if (strValue == null) {
            strValue = Long.toString((Long) defaultVal);
          }
          return (T) new Long(Long.parseLong(strValue));
        }
        else {
          SharedPreferences sharedPreferences =
              context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
          Long defLongValue = (Long) defaultVal;
          Long longValue = sharedPreferences.getLong(key, defLongValue);
          return (T) longValue;
        }
      } else if (defaultVal instanceof Boolean) {
        if (AppConfig.getInstance().isGoBuild()) {
          String strValue = preferenceDao.getPreference(fileName, key);
          if (strValue == null) {
            strValue = Boolean.toString((Boolean) defaultVal);
          }
          return (T) new Boolean(Boolean.parseBoolean(strValue));
        }
        else {
          SharedPreferences sharedPreferences =
              context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
          Boolean defBooleanValue = (Boolean) defaultVal;
          Boolean booleanValue = sharedPreferences.getBoolean(key, defBooleanValue);
          return (T) booleanValue;
        }
      } else if (defaultVal instanceof Set) {
        SharedPreferences sharedPreferences =
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        Set<String> setValue = sharedPreferences.getStringSet(key, (Set<String>) defaultVal);
        return (T) setValue;
      } else if (defaultVal instanceof Float) {
        if (AppConfig.getInstance().isGoBuild()) {
          String strValue = preferenceDao.getPreference(fileName, key);
          if (strValue == null) {
            strValue = Float.toString((Float) defaultVal);
          }
          return (T) new Float(Float.parseFloat(strValue));
        }
        else {
          Float defFloatValue = (Float) defaultVal;
          SharedPreferences sharedPreferences =
              context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
          Float floatValue = sharedPreferences.getFloat(key, defFloatValue);
          return (T) floatValue;
        }
      }
    } catch (Exception e) {
      remove(key, preferenceType);
      Logger.e(TAG, "Removed pref:"+key+", "+preferenceType, e);
      if(Logger.loggerEnabled()) throw e;
    }

    return defaultVal;
  }

  public static String getString(String key) {
    return getString(key, Constants.EMPTY_STRING);
  }

  public static String getString(String key, String defaultValue) {
    return getPreference(null, key, defaultValue);
  }

  public static Set<String> getStringSet(String key, Set<String> defaultValue) {
    return getPreference(null, key, defaultValue);
  }

  public static int getInt(String key) {
    return getInt(key, Integer.MIN_VALUE);
  }

  public static int getInt(String key, int defaultValue) {
    return getPreference(null, key, defaultValue);
  }

  public static float getFloat(String key, float defaultValue) {
    return getPreference(null, key, defaultValue);
  }

  public static long getLong(String key) {
    return getLong(key, Long.MIN_VALUE);
  }

  public static long getLong(String key, long defaultValue) {
    return getPreference(null, key, defaultValue);
  }

  public static boolean getBoolean(String key, boolean defaultValue) {
    return getPreference(null, key, defaultValue);
  }

  public static void remove(String key) {
    remove(key, null);
  }

  public static void remove(SavedPreference savedPreference) {
    remove(savedPreference.getName(), savedPreference.getPreferenceType());
  }

  private static void remove(String key, PreferenceType preferenceType) {
    String fileName = key;
    if (preferenceType != null) {
      fileName = preferenceType.getFileName();
    }
    if (AppConfig.getInstance().isGoBuild()) {
      preferenceDao.remove(fileName, key);
    }
    else {
      SharedPreferences sharedPreferences =
          CommonUtils.getApplication().getSharedPreferences(fileName, Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.remove(key);
      editor.apply();
    }
  }

}
