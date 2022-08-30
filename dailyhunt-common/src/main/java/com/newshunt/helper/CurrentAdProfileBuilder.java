package com.newshunt.helper;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.PasswordEncryption;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.info.LocationInfoHelper;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.status.CurrentAdProfile;
import com.newshunt.dhutil.helper.preference.AdsPreference;

/**
 * Created by karthik.r on 2019-08-26.
 */
public class CurrentAdProfileBuilder {

  public static CurrentAdProfile getCurrentAdProfile() {
    CurrentAdProfile profile = new CurrentAdProfile();
    profile.setClientInfo(ClientInfoHelper.getClientInfo());
    profile.setConnectionInfo(ConnectionInfoHelper.getConnectionInfo());
    profile.setLocationInfo(LocationInfoHelper.getLocationInfo(true));
    try {
      profile.setAndroidId(PasswordEncryption.encrypt(ClientInfoHelper.getAndroidId()));
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    profile.setPackageName(CommonUtils.getApplication().getPackageName());
    profile.setMimeTypes(Constants.MIME_TYPES_SUPPORTED);
    profile.setSupplementAdsSupported(true);
    profile.setVersion(PreferenceManager.getPreference(AdsPreference.ADS_CONFIG_VERSION,
        Constants.EMPTY_STRING));
    return profile;
  }

}
