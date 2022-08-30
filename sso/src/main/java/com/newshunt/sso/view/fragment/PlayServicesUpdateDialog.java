/*
 *  *Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.sso.view.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.sso.R;

/**
 * A dialog for telling the user to update their Google play Services app.
 * <p>
 * Created by anshul.jain. on 11/12/2016.
 */

public class PlayServicesUpdateDialog extends DialogFragment {

  private final String PLAY_SERVICES_PLAY_STORE_LINK =
      "https://play.google.com/store/apps/details?id=com.google.android.gms";

  public static PlayServicesUpdateDialog newInstance() {
    PlayServicesUpdateDialog dialog = new PlayServicesUpdateDialog();
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View rootView = inflater.inflate(com.newshunt.common.util.R.layout.google_play_services_update, container, false);

    TextView dialogTitle = (TextView) rootView.findViewById(com.newshunt.common.util.R.id.play_services_dialog_text);
    TextView dialogHeader = (TextView) rootView.findViewById(com.newshunt.common.util.R.id.play_services_dialogHeadertext);
    TextView positiveButton = (TextView) rootView.findViewById(com.newshunt.dhutil.R.id.positive_button);

    positiveButton.setText(CommonUtils.getString(com.newshunt.common.util.R.string.update));
    dialogTitle.setText(CommonUtils.getString(com.newshunt.common.util.R.string.sign_in));
    dialogHeader.setText(CommonUtils.getString(com.newshunt.common.util.R.string.play_services_dialog_message));

    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    positiveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
        openGooglePlayStore();
      }
    });
    return rootView;
  }

  /**
   * Open the Google Play services in the play store.
   */
  private void openGooglePlayStore() {
    Activity activity = getActivity();
    if (activity == null) {
      return;
    }
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setData(Uri.parse(PLAY_SERVICES_PLAY_STORE_LINK));
    boolean doesActivityExist =
        (intent.resolveActivity(activity.getPackageManager()) != null);
    if (doesActivityExist) {
      activity.startActivity(intent);
    } else {
      FontHelper.showCustomFontToast(activity, CommonUtils.getString(com.newshunt.common.util.R.string.unexpected_error_message),
          Toast.LENGTH_LONG);
    }
  }
}
