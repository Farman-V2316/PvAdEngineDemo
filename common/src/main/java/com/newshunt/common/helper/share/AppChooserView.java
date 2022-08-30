/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.newshunt.common.util.R;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;

import java.util.List;

/**
 * Custom dialog to show share icons in news details page.
 *
 * @author sumedh.tambat
 */
public class AppChooserView extends Dialog implements RecyclerViewOnItemClickListener {

  private Context context;
  private List<ShareAppDetails> appDetails;
  private ShareViewShowListener shareViewListener;
  private ShareUi shareUi;

  public AppChooserView(Context context, List<ShareAppDetails> appDetails,
                        ShareViewShowListener shareViewListener, ShareUi shareUi) {
    super(context);
    this.context = context;
    this.appDetails = appDetails;
    this.shareViewListener = shareViewListener;
    this.shareUi = shareUi;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    setContentView(R.layout.app_chooser);

    RecyclerView appListView = (RecyclerView) findViewById(R.id.app_chooser_view);
    appListView.setHasFixedSize(true);
    appListView.setLayoutManager(new LinearLayoutManager(context));

    AppChooserListAdapter appChooserAdapter = new AppChooserListAdapter(context, appDetails, this);
    appChooserAdapter.notifyDataSetChanged();
    appListView.setAdapter(appChooserAdapter);
  }

  @Override
  public void onItemClick(Intent intent, int position) {
    setDefaultShareList(position);
    if (null != shareViewListener) {
      shareViewListener.onShareViewClick(appDetails.get(position).getAppPackage(), shareUi);
    }
    dismiss();
  }

  private void setDefaultShareList(int position) {
    String defaultShareApps =
        PreferenceManager.getPreference(GenericAppStatePreference.SHARE_APP_OPTIONS, "");

    //if last used app is the same as the last app then don't do anything
    if (defaultShareApps.contains(appDetails.get(position).getAppPackage())) {
      return;
    }
    String subString[] = defaultShareApps.split("\\" + Constants.PIPE_CHARACTER);
    for (int i = 0; i < subString.length - 1; i++) {
      subString[i] = subString[i + 1];
    }

    subString[subString.length - 1] = appDetails.get(position).getAppPackage();
    defaultShareApps = TextUtils.join(Constants.PIPE_CHARACTER, subString);

    PreferenceManager.savePreference(GenericAppStatePreference.SHARE_APP_OPTIONS, defaultShareApps);
  }
}
