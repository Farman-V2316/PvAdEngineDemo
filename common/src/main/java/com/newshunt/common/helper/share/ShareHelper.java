package com.newshunt.common.helper.share;

import android.app.Activity;
import android.content.Intent;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;

/**
 * Interface for app specific share
 *
 * @author ambar
 */
public abstract class ShareHelper{
  public String packageName;
  public Activity activity;
  public Intent sendIntent;
  // shareContent is null in case of application share
  public ShareContent shareContent;
  // appendShareSource is false in case of NHBrowser, share source is not added
  public boolean appendShareSource;
  //platform dependent share type
  public static final String PLATFORM_DEFAULT_SHARE_TYPE = "platform_default";

  ShareHelper(String packageName, Activity activity, Intent sendIntent,
              ShareContent shareContent, boolean appendShareSource) {
    this.packageName = packageName;
    this.activity = activity;
    this.sendIntent = sendIntent;
    this.shareContent = shareContent;
    this.appendShareSource = appendShareSource;
  }

  public abstract void share();

  protected void startShareActivity() {
    if (null != sendIntent) {
      sendIntent.setPackage(packageName);
      Intent intent = sendIntent;
      sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      if (CommonUtils.isEmpty(packageName)) {
        intent = Intent.createChooser(sendIntent, Constants.INTENT_MESSAGE);
      }
      AndroidUtils.startActivity(activity, intent);
    }
  }

  protected void startShareActivityForResult() {
    if (null != sendIntent) {
      sendIntent.setPackage(packageName);
      Intent intent = sendIntent;
      if (CommonUtils.isEmpty(packageName)) {
        intent = Intent.createChooser(sendIntent, Constants.INTENT_MESSAGE);
      }
      AndroidUtils.startSharingActivityForResult(activity, intent);
    }
  }

}
