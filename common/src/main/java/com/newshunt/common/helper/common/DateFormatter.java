/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Formats the long time in "ago" pattern.
 *
 * @author nilesh.borkar
 */
public class DateFormatter {

  public static final int SECOND_MILLIS = 1000;
  public static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
  public static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
  public static final int DAY_MILLIS = 24 * HOUR_MILLIS;
  public static final String PUBLISH_DATE_FORMAT = "d MMM yyyy \u00b7 h:mmaa";
  public static final String PUBLISH_DATE_TIME_FORMAT = "h:mmaa  \u00b7 d MMM yyyy";

  public static final String FORMAT_MONTH_YEAR = "MMM yyyy";

  public static final String FORMAT_DATE_MONTH_YEAR = "dd MMMM yyyy";

  public static final String FORMAT_DD_MONTH_YEAR = "dd MMM yyyy";

  public static String getTimeAgo(long time) {
    return getTimeAgo(time, System.currentTimeMillis());
  }

  public static String getTimeAgo(long time, long now) {

    if (time < 1000000000000L) {
      time *= 1000;
    }

    if (time <= 0) {
      return null;
    }

    if (time > now) {
      return "now";
    }

    final long diff = now - time;
    if (diff < MINUTE_MILLIS) {
      return "now";
    } else if (diff < 60 * MINUTE_MILLIS) {
      return diff / MINUTE_MILLIS + "m";
    } else if (diff < 24 * HOUR_MILLIS) {
      return diff / HOUR_MILLIS + "h";
    } else {
      return diff / DAY_MILLIS + "d";
    }
  }

  public static String getTimeAgoRoundedToMinute(long time) {
    return getTimeAgoRoundedToMinute(time, System.currentTimeMillis());
  }

  public static String getTimeAgoRoundedToMinute(long time, long now) {
    if (time <= 0) {
      return null;
    }

    if (time > now) {
      return "now";
    }

    final long diff = now - time;
    return getTimeByDiff(diff);
  }

  public static String getTimeByDiff(long diff) {
    if (diff <= MINUTE_MILLIS / 2) {
      return "now";
    } else if (diff < 60 * MINUTE_MILLIS) {
      String timeMin = CommonUtils.roundToNearest(diff, MINUTE_MILLIS, true) / MINUTE_MILLIS + "m";
      return timeMin.equals("60m") ? "1h" : timeMin;
    } else if (diff < 24 * HOUR_MILLIS) {
      return diff / HOUR_MILLIS + "h";
    } else {
      return diff / DAY_MILLIS + "d";
    }
  }

  public static String getDateTimeByDiff(long diff) {
//    Handling less than 30 seconds
    if (diff <= MINUTE_MILLIS / 2) {
      return "now";
    } else if (diff < 60 * MINUTE_MILLIS) {
      String timeMin = CommonUtils.roundToNearest(diff, MINUTE_MILLIS, true) / MINUTE_MILLIS + "m"; // Finding the rounded of Minutes
      return timeMin.equals("60m") ? "1h" : timeMin;
    } else if (diff < 24 * HOUR_MILLIS) {
      long hours =  diff / HOUR_MILLIS; // Getting the hours
      String minutes = getDateTimeByDiff(diff%HOUR_MILLIS); // getting the remaining minutes, either "now" or "1h" or "Xm" will be returned
      if (minutes.equals("1h")) {
        return ((hours+1) == 24) ? "1d" : (hours+1)+"h"; // If 1h then add it to hours to check if it is equal to 24 hours
      }
      else {
        return minutes.equals("now") ? hours + "h" : hours + "h " + minutes; // If "now" simply return hours else return hours and minutes
      }
    } else {
      long days =  diff / DAY_MILLIS; // Similar to hours, get the days.
      String hours = getDateTimeByDiff(diff%DAY_MILLIS); // getting the hours now. Either "now "or "1d" or "1h" or "Xm" or "Xh" or "Xh Ym" will be returned.
      if (hours.equals("now")) {
        return days+"d"; // if remaining is rounded to "now" simply return days
      } else if(hours.equals("1d"))
      {
        return (days+1)+"d"; // if "1d" is returned need to add it in days to update the number of days.
      }
      else {
        return days+"d "+hours; // in all the other cases simply append and return.
      }
    }
  }

  private static long getCurrentTime() {
    return System.currentTimeMillis();
  }

  public static CharSequence getPublishDateAsString(long publishTime) {
    SimpleDateFormat sdf = new SimpleDateFormat(PUBLISH_DATE_FORMAT, Locale.ENGLISH);

    DateFormatSymbols symbols = new DateFormatSymbols(Locale.ENGLISH);
    symbols.setAmPmStrings(new String[]{" am", " pm"});
    sdf.setDateFormatSymbols(symbols);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(publishTime);
    return sdf.format(calendar.getTime());
  }

  public static CharSequence getPublishTimeDateAsString(long publishTime) {
    SimpleDateFormat sdf = new SimpleDateFormat(PUBLISH_DATE_TIME_FORMAT, Locale.ENGLISH);

    DateFormatSymbols symbols = new DateFormatSymbols(Locale.ENGLISH);
    symbols.setAmPmStrings(new String[]{" am", " pm"});
    sdf.setDateFormatSymbols(symbols);

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(publishTime);
    return sdf.format(calendar.getTime());
  }

  public static String getFormattedDateString(long milliSeconds, String format) {
    SimpleDateFormat formatter = new SimpleDateFormat(format);
    String formattedDate = formatter.format(milliSeconds);
    return formattedDate.toUpperCase();
  }
}
