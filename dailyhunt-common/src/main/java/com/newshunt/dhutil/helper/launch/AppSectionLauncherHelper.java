/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.launch;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dataentity.dhutil.model.entity.launch.TimeWindow;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchConfigResponse;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchRule;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * A Helper function to apply server configured rules and to launch to configured app section
 * {@link AppSection}
 *
 * @author santhosh.kc
 */
public class AppSectionLauncherHelper {

  private static final String PREVIOUS = "previous";

  /**
   * Helper function to get server configured app section.
   *
   * This function will apply rule one by one and find the matching rule, then return
   * {@link UserAppSection} mentioning {@link AppSection} type,
   * String appSectionId, String entityKeyInAppSection to launch
   *
   * @param appLaunchConfigResponse - ServerConfiguredRulesResponse
   * @param appSectionInfos - List of ServerConfiguredSections
   * @return - {@link UserAppSection} to launch
   */
  public static UserAppSection getNextLaunchSection(
      AppLaunchConfigResponse appLaunchConfigResponse, List<AppSectionInfo> appSectionInfos) {
    if (appLaunchConfigResponse == null ||
        CommonUtils.isEmpty(appLaunchConfigResponse.getLaunchRules())) {
      return null;
    }

    AppSectionInfo appSectionInfoToLaunch = null;
    AppLaunchRule ruleApplied = null;

    List<AppLaunchRule> rules = appLaunchConfigResponse.getLaunchRules();
    for (AppLaunchRule rule : rules) {
      appSectionInfoToLaunch = applyRule(appSectionInfos, rule);
      if (appSectionInfoToLaunch != null && appSectionInfoToLaunch.getType().isLandingSupported()) {
        //specifically skipping the rule, if this section does not support direct landing
        ruleApplied = rule;
        break;
      }
    }

    if (ruleApplied == null) {
      return null;
    }

    return new UserAppSection.Builder().section(appSectionInfoToLaunch.getType())
        .sectionId(appSectionInfoToLaunch.getId())
        .sectionContentUrl(appSectionInfoToLaunch.getContentUrl())
        .entityKey(getEntityKeyInSectionToLaunch(ruleApplied, appSectionInfoToLaunch)).build();
  }

  /*
   * Applying launch rule
   * 1. First the check if current time is within starttime and end time of the rule, else return
   * at this point.
   *
   * 2. then get AppSectionInfo of previous userAppSection. If AppSectionInfo of previous
   * userAppSection is not available(case, where user exited section is not present in server
   * configured bottombar), then return at this point.
   *
   * 3. if prevAppSection is available, then run the previousSectionId against the list of
   * previous section Ids mentioned in the rule.If not found, return null at this point.
   *
   * 4. If nextSectionId mentioned in the rule is "previous", return previousAppSectionInfo found
   * at Step2.
   *
   * 5. If nextSectionId in the rule is not "previous", run the nextSectionId against the list of
   * server available AppSectionInfo, return the matching {@link AppSectionInfo} else null
   *
   * @param appSectionInfos - List of server configured available {@link AppSectionInfo}s
   * @param rule - {@link AppLaunchRule} to apply
   * @return - matching {@link AppSectionInfo} to launch
   */
  private static AppSectionInfo applyRule(List<AppSectionInfo> appSectionInfos,
                                          AppLaunchRule rule) {
    UserAppSection prevAppSection = AppUserPreferenceUtils.getPreviousAppSection();
    //Step1: checking for timebound and negative cases.
    if (rule == null || prevAppSection == null || CommonUtils.isEmpty(appSectionInfos) ||
        !CommonUtils.isCurrentTimeInBounds(rule.getStartTime(), rule.getEndTime()) ||
        !ruleFitsInTimeWindow(rule)) {
      return null;
    }

    //Step2: Get AppSectionInfo of previous userAppSection.
    AppSectionInfo prevAppSectionInfo = getPrevAppSectionInfo(appSectionInfos,prevAppSection);
    //If AppSectionInfo of previous userAppSection is not available(case, where user exited
    // section is not present in server configured bottombar), then return at this point.
    if (prevAppSectionInfo == null) {
      return null;
    }
    String prevAppSectionId = prevAppSectionInfo.getId();

    //Step3: Run the id of previous app section against the list of previous app sections
    // mentioned in the rule, if not found, then return at this point.
    if (!isFoundInServerConfiguredPrevSections(rule.getPreviousSections(), prevAppSectionId)) {
      return null;
    }

    //Step 4 and 5
    return getMatchingAppSectionInfo(appSectionInfos, rule, prevAppSectionInfo);
  }

  private static AppSectionInfo getMatchingAppSectionInfo(List<AppSectionInfo> appSectionInfos,
                                                          AppLaunchRule rule,
                                                          AppSectionInfo prevAppSectionInfo) {
    if (rule == null || CommonUtils.isEmpty(appSectionInfos)) {
      return null;
    }

    //step 4:If nextSectionId mentioned in the rule is "previous", return previousAppSectionInfo
    // found
    // at Step2
    if (CommonUtils.equals(rule.getNextSection(), PREVIOUS)) {
      return prevAppSectionInfo;
    }

    // if the current time is expired and then next section is available then launch the section
    long lastExitTime = System.currentTimeMillis();
    if (prevAppSectionInfo.getType() == AppSection.TV) {
      lastExitTime = PreferenceManager.getPreference(AppStatePreference.BUZZ_EXIT_TIME,
          lastExitTime);
    }
    long diffTime = (System.currentTimeMillis() - lastExitTime)/1000;
    if (rule.getExpiryTime() > 0 && !CommonUtils.isEmpty(rule.getNextSectionAfterExpiry())
        && diffTime > rule.getExpiryTime()) {
      for (AppSectionInfo sectionInfo : appSectionInfos) {
        if (CommonUtils.equals(rule.getNextSectionAfterExpiry(), sectionInfo.getId())) {
          return sectionInfo;
        }
      }
    }

    //Step 5: run the nextSectionId against the list of server available AppSectionInfo, return
    // the matching {@link AppSectionInfo} else null
    AppSectionInfo matchingAppSectionInfo = null;
    for (AppSectionInfo sectionInfo : appSectionInfos) {
      if (CommonUtils.equals(rule.getNextSection(), sectionInfo.getId())) {
        matchingAppSectionInfo = sectionInfo;
        break;
      }
    }
    return matchingAppSectionInfo;
  }

  /**
   * Helper function to get matching {@link AppSectionInfo} matching the previous UserAppSection.
   * On app upgrade case, since section id won't be present, we will return the matching
   * AppSectionInfo based on {@link AppSection} type
   *
   * @param appSectionInfos - list of {@link AppSectionInfo} to run against
   * @param prevAppSection - input appSection
   * @return - matching {@link AppSectionInfo}
   */
  public static AppSectionInfo getPrevAppSectionInfo(List<AppSectionInfo> appSectionInfos,
                                                     UserAppSection prevAppSection) {
    if (prevAppSection == null || CommonUtils.isEmpty(appSectionInfos)) {
      return null;
    }

    AppSectionInfo prevAppSectionInfo = null;
    String prevAppSectionId = prevAppSection.getId();
    for (AppSectionInfo appSectionInfo : appSectionInfos) {
      //app upgrade case handling.
      if (CommonUtils.isEmpty(prevAppSectionId) && prevAppSection.getType() == appSectionInfo.getType()) {
        prevAppSectionInfo = appSectionInfo;
        break;
      }
      if (CommonUtils.equals(appSectionInfo.getId(), prevAppSectionId)) {
        prevAppSectionInfo = appSectionInfo;
        break;
      }
    }
    return prevAppSectionInfo;
  }

  private static boolean isFoundInServerConfiguredPrevSections(List<String>
                                                                   serverConfiguredPrevSections,
                                                               String userPrevSection) {
    if (CommonUtils.isEmpty(userPrevSection) || CommonUtils.isEmpty(serverConfiguredPrevSections)) {
      return false;
    }

    for (String serverPrevId : serverConfiguredPrevSections) {
      if (CommonUtils.equals(PREVIOUS, serverPrevId) || CommonUtils.equals(serverPrevId, userPrevSection)) {
        return true;
      }
    }
    return false;
  }

  private static String getEntityKeyInSectionToLaunch(AppLaunchRule ruleApplied, AppSectionInfo
      appSectionInfoToLaunch) {
    if (ruleApplied == null || appSectionInfoToLaunch == null) {
      return Constants.EMPTY_STRING;
    }

    String entityKeyToLaunch = ruleApplied.getNextSectionEntity();
    if (CommonUtils.isEmpty(entityKeyToLaunch) || CommonUtils.equals(entityKeyToLaunch, PREVIOUS)) {
      UserAppSection prevAppSection = AppSectionsProvider.INSTANCE.getLastVisitedInfo
          (appSectionInfoToLaunch.getId());
      entityKeyToLaunch = prevAppSection == null ? Constants.EMPTY_STRING : prevAppSection
          .getAppSectionEntityKey();
    }

    return entityKeyToLaunch;
  }

  /**
   * Check the Time windows in the app launch rule to see if the current time falls within one of
   * the windows.
   * @param rule AppLaunchRule config from server
   * @return true if the rule can be applied, false otherwise
   */
  public static boolean ruleFitsInTimeWindow(@NotNull AppLaunchRule rule) {
    //If no time windows are specified, this rule can be applied
    if (CommonUtils.isEmpty(rule.getTimeWindows())) {
      return true;
    }
    //Calculate the device time w.r.t timezone in milliseconds
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    long currentTimeInMillis = TimeUnit.HOURS.toMillis(calendar.get(Calendar.HOUR_OF_DAY)) +
        TimeUnit.MINUTES.toMillis(calendar.get(Calendar.MINUTE)) +
        TimeUnit.SECONDS.toMillis(calendar.get(Calendar.SECOND)) +
        calendar.get(Calendar.MILLISECOND);

    //Loop through each time window and check if the current time falls in one of the windows and return true
    for (TimeWindow window : rule.getTimeWindows()) {
      if (currentTimeInMillis >= window.getStartTimeMs() &&
          currentTimeInMillis < window.getEndTimeMs()) {
        return true;
      }
    }
    return false;
  }
}
