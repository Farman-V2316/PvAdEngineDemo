/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.client;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.info.LocationInfoHelper;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.status.ConnectionInfo;
import com.newshunt.dataentity.common.model.entity.status.LocationInfo;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.sdk.network.connection.ConnectionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides utility methods to build event.
 *
 * @author shreyas.desai
 */
public class NhAnalyticsEventHelper {

  public static Map<NhAnalyticsEventParam, Object> getBaseParams(String clientId) {
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();

    String primaryLanguage = AppUserPreferenceUtils.getUserPrimaryLanguage();
    if (!DataUtil.isEmpty(primaryLanguage)) {
      eventParams.put(NhAnalyticsAppEventParam.PRIMARY_LANGUAGE, primaryLanguage);
    }

    String secondaryLanguage = AppUserPreferenceUtils.getUserSecondaryLanguages();
    if (!DataUtil.isEmpty(secondaryLanguage)) {
      eventParams.put(NhAnalyticsAppEventParam.SECONDARY_LANGUAGE, secondaryLanguage);
    }

    ConnectionInfo connectionInfo = ConnectionInfoHelper.getConnectionInfo();
    eventParams.put(NhAnalyticsAppEventParam.USER_CONNECTION, connectionInfo.getConnection());
    eventParams.put(NhAnalyticsAppEventParam.USER_CONNECTION_QUALITY,
        ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication()).name());

    eventParams.put(NhAnalyticsAppEventParam.CELL_ID, connectionInfo.getCellid());

    LocationInfo locationInfo = LocationInfoHelper.getLocationInfo(false);
    eventParams.put(NhAnalyticsAppEventParam.LATITUDE, locationInfo.getLat());
    eventParams.put(NhAnalyticsAppEventParam.LONGITUDE, locationInfo.getLon());
    if (!DataUtil.isEmpty(clientId)) {
      eventParams.put(NhAnalyticsAppEventParam.CLIENT_ID, clientId);
    }
    eventParams.put(NhAnalyticsAppEventParam.CLIENT_GENERATED_CLIENT_ID, ClientInfoHelper.getClientGeneratedClientId());

    Boolean isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED,
        false);

    eventParams.put(NhAnalyticsAppEventParam.IS_REGISTERED, isRegistered);
    String userLoginType =
        PreferenceManager.getPreference(GenericAppStatePreference.USER_LOGIN_TYPE,
            Constants.EMPTY_STRING);
    LoginType loginType = LoginType.fromValue(userLoginType);
    if (loginType != null && loginType.isSocial()) {
      eventParams.put(NhAnalyticsAppEventParam.SIGNIN_MEDIUM, userLoginType);
      eventParams.put(NhAnalyticsAppEventParam.SIGNED_STATE, Constants.SIGNED_IN);
    } else {
      eventParams.put(NhAnalyticsAppEventParam.SIGNED_STATE, Constants.SIGNED_OUT);
    }

    eventParams.put(NhAnalyticsAppEventParam.USER_TYPE, AppUserPreferenceUtils.getUserType());
    eventParams.put(NhAnalyticsAppEventParam.WIFI_SSID, ConnectionInfoHelper.getWifiSsid());
    String wifiMac = PreferenceManager.getPreference(GenericAppStatePreference.WIFI_MAC_ADDRESS, Constants.EMPTY_STRING);
    if (!CommonUtils.isEmpty(wifiMac)) {
      eventParams.put(NhAnalyticsAppEventParam.WIFI_MAC, wifiMac);
    }
    String currentPackageName = CommonUtils.getApplication().getPackageName();
    if (!Constants.DAILYHUNT_PACKAGE.equals(currentPackageName)) {
      eventParams.put(NhAnalyticsAppEventParam.PACKAGE_NAME, currentPackageName);
    }
    if(!CommonUtils.isEmpty(AppUserPreferenceUtils.getAppsFlyerUID())){
      eventParams.put(NhAnalyticsAppEventParam.APPSF_TRACKING_ID, AppUserPreferenceUtils.getAppsFlyerUID());
    }

    String location = PreferenceManager.getPreference(AppStatePreference.SERVER_LOCATION,Constants.EMPTY_STRING);
    eventParams.put(NhAnalyticsAppEventParam.SERVER_LOC,location);
    return eventParams;
  }
}
