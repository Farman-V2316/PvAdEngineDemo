package com.newshunt.dhutil.helper.preference;

import com.newshunt.common.helper.preference.PreferenceType;
import com.newshunt.common.helper.preference.SavedPreference;

/**
 * Preferences related to app rate.
 *
 * @author Shashikiran.nr
 */
public enum AppRatePreference implements SavedPreference {
  // Ability to enable disable APPRATING feature as a whole
  IS_APPRATING_DIALOG_ENABLED("is_apprate_dialog_enabled", PreferenceType.APP_RATE),
  // Session pre-activity: 10x60 seconds
  APPRATE_MAX_SESSION_WAIT_TIME_SECONDS("apprate_max_session_wait_time_seconds",
      PreferenceType.APP_RATE),
  // Number of stories viewed in a session: 10
  APPRATE_MIN_STORIES_VIEWED_PER_SESSION("apprate_min_stories_viewed_per_session",
      PreferenceType.APP_RATE),
  // Number of stories shared in a session: 5
  APPRATE_MIN_STORIES_SHARED("apprate_min_stories_shared",
          PreferenceType.APP_RATE),
  // Subsequent number of stories seen to show rate us dialog : 5
  APPRATE_SUBSEQUENT_STORIES_SHARED("apprate_subsequent_stories_shared",
          PreferenceType.APP_RATE),
  // X Number of books read (exits book reader 4th time)
  APPRATE_MIN_BOOKS_READ("apprate_min_books_read", PreferenceType.APP_RATE),
  // x Number of times to show
  APPRATE_MAX_TIMES_SHOW("apprate_max_times_show", PreferenceType.APP_RATE),
  // X Number of days to wait to show
  APPRATE_MIN_DAYS_WAIT_SHOW("apprate_min_days_wait_show", PreferenceType.APP_RATE),
  // X Number days to wait to show new users
  APPRATE_MAX_WAIT_DAYS_NEWUSERS_SHOW("apprate_max_wait_days_newusers_show",
      PreferenceType.APP_RATE),
  // X Number launches to wait to show new users
  APPRATE_MIN_LAUNCHES_NEWUSERS_SHOW("apprate_min_launches_newusers_show", PreferenceType.APP_RATE),
  // X Number days to wait to show if already shown
  APPRATE_MIN_DAYS_USER_AFTER_SHOWN("apprate_min_days_user_after_shown", PreferenceType.APP_RATE),
  // X Number launches to wait to show if already shown
  APPRATE_MIN_APP_LAUNCHES_AFTER_SHOWN("apprate_min_app_launches_after_shown",
      PreferenceType.APP_RATE),
  // Is user clicked on Rate Now in App Rate Dialog
  APPRATE_IS_USER_CLICKED_RATE_NOW("apprate_is_user_clicked_rate_now", PreferenceType.APP_RATE),
  // never show rate app doalog
  APPRATE_NEVER_SHOW_AGAIN("apprate_never_show_again", PreferenceType.APP_RATE),
  // Is App rate dialog already shown
  IS_APPRATE_DIALOG_SHOWN("is_apprate_dialog_shown", PreferenceType.APP_RATE),
  // App launch count
  APP_LAUNCH_COUNT("app_launch_count", PreferenceType.APP_RATE),
  // App launch count after upgrade
  APP_LAUNCH_COUNT_AFTER_UPGRADE("app_launch_count_after_upgrade", PreferenceType.APP_RATE),
  // last app version after upgrade
  LAST_APP_VERSION_AFTER_UPGRADE("last_app_version_after_upgrade", PreferenceType.APP_RATE),
  // last app version after upgrade
  RATE_SCREEN_SHOWN_AFTER_UPGRADE("rate_screen_shown_after_upgrade", PreferenceType.APP_RATE),
  // App rate show count
  APPRATE_SHOW_COUNT("apprate_show_count", PreferenceType.APP_RATE),
  // App rate show last start date
  APPRATE_SHOW_START_DATE("apprate_show_start_date", PreferenceType.APP_RATE),
  // App first launch time or latest Upgrade time
  FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME("FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME",
      PreferenceType.APP_RATE),
  // Story read count
  STORY_VIEWED_COUNT_PER_SESSION("story_viewed_count_per_session", PreferenceType.APP_RATE),
  // Story share count per session
  STORY_SHARED_COUNT("story_shared_count",PreferenceType.APP_RATE),
  // Book read count
  BOOK_READ_COUNT("BOOK_READ_COUNT", PreferenceType.APP_RATE),
  // App launch count after upgrade
  APPRATE_MIN_APP_LAUNCHES_AFTER_UPGRADE("apprate_min_app_launches_after_upgrade",PreferenceType.APP_RATE);


  private String name;
  private PreferenceType preferenceType;

  AppRatePreference(String name, PreferenceType preferenceType) {
    this.name = name;
    this.preferenceType = preferenceType;
  }

  @Override
  public PreferenceType getPreferenceType() {
    return preferenceType;
  }

  @Override
  public String getName() {
    return name;
  }
}
