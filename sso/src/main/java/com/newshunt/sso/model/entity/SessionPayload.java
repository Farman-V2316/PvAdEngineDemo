/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.sso.SSO;
import com.newshunt.sso.helper.GuestUserNamePasswordGenerator;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Stores Login Details of User as userLoginPayload or socialLoginPayload with TimeStamp, TimeZone
 * loginPayload --> can be Guest or E-mail Login details from UserLoginPayload or
 * Social Login details of Facebook or Google+
 * TimeStamp and TimeZone is added to ensure authentication within some time range
 * Server will evaluate session data to extend based session on timestamp & timezone.
 *
 * @author vinod.bc
 */
public class SessionPayload {

  private Object loginPayload;
  private final long timeStamp;
  private final String timeZone;

  public SessionPayload(SSO.UserDetails userDetails) {

    Credential credential =
        GuestUserNamePasswordGenerator.getCredentials(ClientInfoHelper.getClientId());

    switch (userDetails.getLoginType()) {
      case NONE:
      case GUEST:
        //Incase of Guest login, userid and password not required so setting to null
        loginPayload =
            new UserLoginPayload(null, null, credential.getUserId(), credential.getPassword(),
                UserExplicit.NO.getValue());
        break;
      case FACEBOOK:
        loginPayload = new SocialLoginPayload(LoginType.FACEBOOK
            .getValue(), Constants.EMPTY_STRING, credential.getUserId(),
            credential.getPassword(), UserExplicit.NO.getValue());
        break;
      case GOOGLE:
        loginPayload = new SocialLoginPayload(LoginType.GOOGLE
            .getValue(), Constants.EMPTY_STRING, credential.getUserId(),
            credential.getPassword(), UserExplicit.NO.getValue(), Constants.EMPTY_STRING);
        break;
    }

    timeStamp = System.currentTimeMillis();
    timeZone = getCurrentTimezoneOffset();
  }

  /*
   * Function referenced from stackoverflow to get GMT Offset across always.
   * http://stackoverflow.com/questions/15068113/how-to-get-the-timezone-offset-in-gmtlike-gmt700-from-android-device
   */
  private String getCurrentTimezoneOffset() {
    TimeZone tz = TimeZone.getDefault();
    Calendar cal = GregorianCalendar.getInstance(tz);
    int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

    String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000),
        Math.abs((offsetInMillis / 60000) % 60));
    offset = "GMT" + (offsetInMillis >= 0 ? "+" : "-") + offset;
    return offset;
  }

}
