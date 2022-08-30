/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.launch;

import androidx.annotation.NonNull;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DateFormatter;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.dhutil.model.entity.EntityConfiguration;
import com.newshunt.dataentity.dhutil.model.entity.EntityItemConfig;
import com.newshunt.dataentity.dhutil.model.entity.language.CampaignLanguageEvent;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class to parse and fetch campaign and reco parameters. These parameters could be coming
 * in from Appsflyer, Firebase etc.
 * <p>
 * Created by srikanth.ramaswamy on 08/23/18.
 */
public class CampaignAcquisitionHelper {

  private static final String LANGS_PARAM = "lang:";
  private static final String LOG_TAG = "CampaignAcquisitionHelper";
  private static final String RECO_CAMPAIGN_PARAMS_PREFIX = "n:";
  private static final String ACQ_PARAM = "acq:";

  //Private constructor. Class can't be instantiated.
  private CampaignAcquisitionHelper() {
  }

  /**
   * Parse the campaign param string, extract the language and send a Bus event to auto land the
   * user to headlines with the campaign's language setting.
   * General format of Campaign parameter:
   * "n:AppInstalls_Karnataka_Elections;lang:en,hi,kn;action:lookup;type:np"
   *
   * @param campaignParam Campaign param string
   */
  public static void parseCampaignParameter(@NonNull final String campaignParam) {
    //First check if the campaign parameter is in the format we support
    if (!isRecoParamsValid(campaignParam)) {
      return;
    }
    //Save the reco params into preference, to be used later.
    final long timeStamp = System.currentTimeMillis();
    PreferenceManager.savePreference(AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS, campaignParam);
    PreferenceManager.savePreference(AppStatePreference
        .ACQUISITION_CAMPAIGN_PARAMS_RECEIVED_TIMESTAMP, timeStamp);
    //Fetch the languages from reco params and send a bus event if languages are configured.
    List<String> languages = fetchLanguagesFromCampaign(campaignParam);
    Logger.d(LOG_TAG, "Saved the campaign Param: " + campaignParam + " @ time: " + timeStamp);
    if (!CommonUtils.isEmpty(languages)) {
      Logger.d(LOG_TAG, "Campaign has languages predefined");
      BusProvider.postOnUIBus(new CampaignLanguageEvent(languages));
    }
  }

  /**
   * Reads the reco params and extracts the languages, returns list of language codes configured
   * in the campaign.
   * @param recoParams reco param string
   * @return List of language codes configured in the campaign
   */
  public static List<String> fetchLanguagesFromCampaign(@NonNull final String recoParams) {
    String[] params = recoParams.split(Constants.SEMICOLON);
    if (!CommonUtils.isEmpty(params)) {
      for (String param : params) {
        int langsStartIndex = param.indexOf(LANGS_PARAM);
        if (langsStartIndex >= 0) {
          langsStartIndex += LANGS_PARAM.length();
          String langParams = param.substring(langsStartIndex);
          if (!CommonUtils.isEmpty(langParams)) {
            String[] languages = langParams.split(Constants.COMMA_CHARACTER);
            if (!CommonUtils.isEmpty(languages)) {
              return Arrays.asList(languages);
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Read the acquisition reco params from preference. If the reco params have expired, clears
   * the preference as well and returns EMPTY_STRING
   * @return reco params
   */
  public static String getAcquisitionRecoParams() {
    if (isRecoParamsExpired(readRecoParamsConfig())) {
      Logger.d(LOG_TAG, "reco params expired, returning EMPTY_STRING");
      resetAcquisitionRecoParams();
      return Constants.EMPTY_STRING;
    }
    return readCampaignAcquisitionParams();
  }

  /**
   * Notify the reco params consumed. Needs to be called after each successful call to reco APIs.
   * Checks the count and time and resets the params if expired.
   */
  public static void recoParamsConsumed() {
    if (CommonUtils.isEmpty(getAcquisitionRecoParams())) {
      Logger.d(LOG_TAG, "recoParamsConsumed: Acquisition params empty, nothing to do");
      return;
    }

    EntityItemConfig recoParamsConfig = readRecoParamsConfig();
    if (recoParamsConfig == null) {
      Logger.d(LOG_TAG, "recoParamsConsumed: Handshake config for reco params is null");
      return;
    }

    int currentCount = PreferenceManager.getPreference(AppStatePreference
        .ACQUISITION_CAMPAIGN_PARAMS_COUNT, 0);
    if ((recoParamsConfig.getCount() > 0 && currentCount >= recoParamsConfig.getCount()) ||
        isRecoParamsExpired(recoParamsConfig)) {
      Logger.d(LOG_TAG, "recoParamsConsumed: reco params expired, resetting");
      resetAcquisitionRecoParams();
      return;
    }
    PreferenceManager.savePreference(AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS_COUNT,
        ++currentCount);
    Logger.d(LOG_TAG, "recoParamsConsumed: Incremented the count "+currentCount);
  }

  public static boolean isRecoParamsValid(final String campaignReferrer) {
    return (!CommonUtils.isEmpty(campaignReferrer) && campaignReferrer.startsWith
        (RECO_CAMPAIGN_PARAMS_PREFIX));
  }

  public static boolean isAcquisitionTypeCampaign(final String campaignReferrer) {
    return isRecoParamsValid(campaignReferrer) && campaignReferrer.contains(ACQ_PARAM);
  }

  public static String readCampaignAcquisitionParams() {
    return PreferenceManager.getPreference(AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS,
        Constants.EMPTY_STRING);
  }

  public static String fetchAcquisitionTypeFromCampaign(final String recoParams) {
    if (!isAcquisitionTypeCampaign(recoParams)) {
      return null;
    }
    String[] params = recoParams.split(Constants.SEMICOLON);
    if (!CommonUtils.isEmpty(params)) {
      for (String param : params) {
        int acqStartIndex = param.indexOf(ACQ_PARAM);
        if(acqStartIndex >= 0) {
          acqStartIndex += ACQ_PARAM.length();
          return param.substring(acqStartIndex);
        }
      }
    }
    return null;
  }

  private static EntityItemConfig readRecoParamsConfig() {
    final String timeConfig =
        PreferenceManager.getPreference(GenericAppStatePreference.THRESHOLD_AND_TIME_CONFIG,
            Constants.EMPTY_STRING);
    if (CommonUtils.isEmpty(timeConfig)) {
      return null;
    }

    EntityConfiguration config = JsonUtils.fromJson(timeConfig, EntityConfiguration.class);
    if (config == null) {
      return null;
    }
    return config.getCampaignRecoParams();
  }

  private static boolean isRecoParamsExpired(final EntityItemConfig recoParamsConfig) {
    return recoParamsConfig == null || (recoParamsConfig.getTimeLimit() != 0 && CommonUtils.isTimeExpired(
        PreferenceManager.getPreference(
            AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS_RECEIVED_TIMESTAMP, 0L),
        DateFormatter.SECOND_MILLIS * recoParamsConfig.getTimeLimit()));
  }

  private static void resetAcquisitionRecoParams() {
    PreferenceManager.remove(AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS);
    PreferenceManager.remove(AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS_COUNT);
    PreferenceManager.remove(AppStatePreference.ACQUISITION_CAMPAIGN_PARAMS_RECEIVED_TIMESTAMP);
    Logger.d(LOG_TAG, "Reset acquisition reco params");
  }
}
