/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.R;

/**
 * To perform action on custom tabs menu item click
 *
 * @author neeraj.kumar
 */
public class CustomTabsBroadcastReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    String url = intent.getDataString();
    AndroidUtils.CopyContent(context, "url", url,
        CommonUtils.getString(com.newshunt.common.util.R.string.copy_to_clipboard));
  }
}
