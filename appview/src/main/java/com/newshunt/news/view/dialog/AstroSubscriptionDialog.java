/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.dialog;

import static com.newshunt.dataentity.common.helper.common.CommonUtils.getTintedDrawable;
import static com.newshunt.news.helper.AstroHelper.setUpBoldTextViewForDialog;
import static com.newshunt.news.helper.AstroHelper.setUpNormalTextViewForDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.newshunt.analytics.entity.DialogBoxType;
import com.newshunt.appview.R;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.view.customview.fontview.NHButton;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.news.view.entity.Gender;
import com.newshunt.dhutil.AstroTriggerAction;
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper;
import com.newshunt.dhutil.helper.preference.AstroPreference;
import com.newshunt.dhutil.helper.theme.ThemeUtils;
import com.newshunt.dhutil.view.customview.IsometricView;
import com.newshunt.news.helper.AstroHelper;
import com.newshunt.news.view.listener.AstroDateSelectedListener;
import com.newshunt.news.view.listener.AstroSubscriptionResultListener;
import com.newshunt.news.view.listener.AstroSubscriptionView;
import com.newshunt.newshome.view.entity.AstroDialogStatus;
import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by anshul on 15/2/17.
 * Dialog for letting the user subscribe to Astro.
 */

public class AstroSubscriptionDialog extends DialogFragment
    implements View.OnClickListener,
    AstroDateSelectedListener, AstroSubscriptionView, Dialog.OnKeyListener {

  private NHButton subscribeButton;
  private NHTextView astroMaleText;
  private NHTextView astroFemaleText;
  private NHTextView astroDateValue;
  //TODO anshul.jain Instead of using a container for wrapping up the dialog, rely on
  // OnDismissListener of the Dialog to consume onDismiss() events
  private FrameLayout astroCrossIconContainer, astroDialogContainer, astroProgressBarContainer;
  private Gender selectedGender = Gender.MALE;
  private FragmentManager fragmentManager;
  private final String TAG = AstroSubscriptionDialog.class.getSimpleName();
  private AstroSubscriptionResultListener astroSubscriptionResultListener;
  private boolean isSubscriptionInProgress = false;
  private Bus uiBus;
  private int uniqueId;
  private IsometricView astroDialog;

  public static AstroSubscriptionDialog newInstance(FragmentManager fragmentManager,
                                                    AstroSubscriptionResultListener
                                                        astroSubscriptionResultListener, Bus
                                                        uiBus, int uniqueId) {
    AstroSubscriptionDialog astroSubscriptionDialog = new AstroSubscriptionDialog();
    astroSubscriptionDialog.setFragmentManager(fragmentManager);
    astroSubscriptionDialog.setAstroSubscriptionResultListener(astroSubscriptionResultListener);
    //Fire analytics event for dialog viewed.
    DialogAnalyticsHelper.deployAstroViewedEvent(NhAnalyticsEventSection.NEWS, DialogBoxType
        .ASTRO_ONBOARDING_PROMPT);
    astroSubscriptionDialog.setUiBus(uiBus);
    astroSubscriptionDialog.setUniqueId(uniqueId);
    return astroSubscriptionDialog;
  }

  public void setUniqueId(int uniqueId) {
    this.uniqueId = uniqueId;
  }

  public void setUiBus(Bus uiBus) {
    this.uiBus = uiBus;
  }

  public void setFragmentManager(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void setAstroSubscriptionResultListener(AstroSubscriptionResultListener
                                                     astroSubscriptionResultListener) {
    this.astroSubscriptionResultListener = astroSubscriptionResultListener;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    View astroView = inflater.inflate(R.layout.dialog_astro, container, false);

    astroDialog = astroView.findViewById(R.id.astro_dialog);
    astroDialog.setOnClickListener(this);
    astroDialogContainer = astroView.findViewById(R.id.astro_dialog_container);
    astroDialogContainer.setOnClickListener(this);
    astroCrossIconContainer = astroView.findViewById(R.id.astro_cross_icon_container);
    astroCrossIconContainer.setOnClickListener(this);

    subscribeButton = astroView.findViewById(R.id.button_astro_subscribe);
    setUpBoldTextViewForDialog(subscribeButton, R.string.astro_subscribe_button_text);
    subscribeButton.setOnClickListener(this);

    //Set the text.
    NHTextView astroSubtitle = astroView.findViewById(R.id.astro_dialog_subtitle);
    setUpNormalTextViewForDialog(astroSubtitle, R.string.astro_dialog_subtitle);
    NHTextView genderDescriptionTextView = astroView.findViewById(R.id.astro_dialog_gender_text);
    setUpNormalTextViewForDialog(genderDescriptionTextView, R.string.astro_gender_text);
    NHTextView dobDescriptionTextView = astroView.findViewById(R.id.astro_dialog_dob_text);
    setUpNormalTextViewForDialog(dobDescriptionTextView, R.string.astro_dob_text);

    astroDateValue = astroView.findViewById(R.id.astro_dialog_dob_value);
    String dateText = getAstroDateString(CommonUtils.getString(R.string.astro_date), CommonUtils.getString(R
        .string.astro_month), CommonUtils.getString(R.string.astro_year));
    astroDateValue.setText((dateText));
    astroDateValue.setOnClickListener(this);

    //Set up the gender containers.
    astroMaleText = astroView.findViewById(R.id.astro_male_text);
    setUpNormalTextViewForDialog(astroMaleText, R.string.astro_gender_male);
    astroMaleText.setOnClickListener(this);
    astroFemaleText = astroView.findViewById(R.id.astro_female_text);
    setUpNormalTextViewForDialog(astroFemaleText, R.string.astro_gender_female);
    astroFemaleText.setOnClickListener(this);
    astroProgressBarContainer = astroView.findViewById(R.id
        .astro_progress_bar_container);

    //Init the data shown when the dialog is displayed.
    setInitialData();

    getDialog().setOnKeyListener(this);
    return astroView;
  }

  @Override
  public void onStart() {
    super.onStart();

    Dialog dialog = getDialog();
    if (dialog != null) {
      dialog.getWindow()
          .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
  }

  private void setInitialData() {
    //UI Init
    setBackgroundToGenderView(AstroHelper.getGender());
    setDateOnSpinner(AstroHelper.getCalendarFromSavedDate());

    if (AstroHelper.canEnableSubscribeButton()) {
      subscribeButton.setEnabled(true);
      subscribeButton.setTextColor(ThemeUtils.getThemeColorByAttribute(getContext(),R.attr.app_rate_submit_text_color));
    }
  }

  private void onGenderSelected(int drawableId, TextView textView) {
    onGenderSelectionChanged(drawableId, textView, R.color.white_color);
  }

  private void onGenderNotSelected(int drawableId, TextView textView) {
    int tintColor = ThemeUtils.isNightMode() ? R.color.white_color : R.color.black_color;
    onGenderSelectionChanged(drawableId, textView, tintColor);
  }

  private void setBackgroundToGenderView(Gender gender) {
    if (gender == null) {
      astroMaleText.setBackgroundResource(R.drawable.astro_gender_unselected);
      astroFemaleText.setBackgroundResource(R.drawable.astro_gender_unselected);
      onGenderNotSelected(R.drawable.vector_astro_male_icon, astroMaleText);
      onGenderNotSelected(R.drawable.vector_astro_female_icon, astroFemaleText);
      return;
    }
    switch (gender) {
      case MALE:
        astroMaleText.setBackgroundResource(R.drawable.astro_gender_selected);
        astroFemaleText.setBackgroundResource(R.drawable.astro_gender_unselected);
        onGenderSelected(R.drawable.vector_astro_male_icon, astroMaleText);
        onGenderNotSelected(R.drawable.vector_astro_female_icon, astroFemaleText);
        break;
      case FEMALE:
        astroMaleText.setBackgroundResource(R.drawable.astro_gender_unselected);
        astroFemaleText.setBackgroundResource(R.drawable.astro_gender_selected);
        onGenderSelected(R.drawable.vector_astro_female_icon, astroFemaleText);
        onGenderNotSelected(R.drawable.vector_astro_male_icon, astroMaleText);
        break;
    }
  }

  public void onGenderSelectionChanged(int drawableId, TextView textView, int colorId) {
    textView.setTextColor(CommonUtils.getColor(colorId));
    Drawable tintedDrawable = getTintedDrawable(drawableId, colorId);
    if (tintedDrawable == null) {
      return;
    }
    int intrinsicHeight = tintedDrawable.getIntrinsicHeight();
    int intrinsicWidth = tintedDrawable.getIntrinsicWidth();
    tintedDrawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
    textView.setCompoundDrawables(tintedDrawable, null, null, null);
  }

  @Override
  public void onClick(View view) {
    //View for dismissing the dialog
    if (view == astroCrossIconContainer || view == astroDialogContainer) {
      handleDialogDismiss();
    }
    // View for selecting date
    else if (view == astroDateValue) {
      onDatePickerClicked();
    }
    // View for selecting the gender as Male
    else if (view == astroMaleText) {
      onGenderSelected(Gender.MALE.getGender());
    }
    //View for setting gender as Female.
    else if (view == astroFemaleText) {
      onGenderSelected(Gender.FEMALE.getGender());
    }
    //Do nothing if someone clicks on the dialog
    else if (view == astroDialog) {
    }
    // Click on subscribe button should make the request
    else if (view == subscribeButton) {
      onSubscribeButtonClicked();
    }
  }

  private void handleDialogDismiss() {
    try {
      dismiss();
    }
    catch (IllegalStateException ex) {
      // Do nothing
    }

    AstroDialogStatus astroDialogStatus = AstroHelper.getAstroDialogStatus();
    //Fire analytics events for cross dismiss
    onAstroCrossButtonClicked();
    switch (astroDialogStatus) {
      case NEVER_SHOWN:
        PreferenceManager.savePreference(AstroPreference.ASTRO_DIALOG_STATUS, AstroDialogStatus
            .DISMISSED_ONCE.getStatus());
        break;
      case DISMISSED_ONCE:
        PreferenceManager.savePreference(AstroPreference.ASTRO_DIALOG_STATUS, AstroDialogStatus
            .DISMISSED_TWICE.getStatus());
        break;
    }
  }

  private void onSubscribeButtonClicked() {
    String gender = selectedGender.getGender();
    String dob =
        PreferenceManager.getPreference(AstroPreference.USER_DOB, Constants.EMPTY_STRING);
    if (CommonUtils.isEmpty(gender) || CommonUtils.isEmpty(dob)) {
      return;
    }
    if (isSubscriptionInProgress) {
      return;
    }
    isSubscriptionInProgress = true;
    onSubscriptionButtonClicked(gender, dob);
  }

  private void setDateOnSpinner(Calendar calendar) {
    if (calendar == null) {
      return;
    }

    int date = calendar.get(Calendar.DAY_OF_MONTH);
    String dateStr = Integer.toString(date);
    if (date < 10) {
      dateStr = 0 + dateStr;
    }
    int year = calendar.get(Calendar.YEAR);
    String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    String astroDateText =
        getAstroDateString(dateStr, month.toUpperCase(), Integer.toString
            (year));
    astroDateValue.setText(astroDateText);
  }

  private String getAstroDateString(String date, String month, String year) {
    return date + Constants.SPACE_STRING +
        month + Constants.SPACE_STRING +
        year;
  }

  @Override
  public void onDateSet(Calendar calendar) {
    AstroHelper.saveUserDateOfBirth(calendar);
    setDateOnSpinner(calendar);
    if (AstroHelper.canEnableSubscribeButton()) {
      subscribeButton.setEnabled(true);
      subscribeButton.setTextColor(ThemeUtils.getThemeColorByAttribute(getContext(),R.attr.app_rate_submit_text_color));
    }
  }


  @Override
  public void onAstroSubscriptionSuccess() {
    isSubscriptionInProgress = false;
    dismiss();
    if (astroSubscriptionResultListener != null) {
      astroSubscriptionResultListener.onAstroSubscriptionSuccess();
    }
  }

  @Override
  public void onAstroSubscriptionFailed(String failureReason) {
    isSubscriptionInProgress = false;
    Logger.d(TAG, "Astro subscription failed because of the following reason " + failureReason);
    if (astroSubscriptionResultListener != null) {
      astroSubscriptionResultListener.onAstroSubscriptionFailed(failureReason);
    }
  }

  @Override
  public void onDatePickerClicked() {
    if(fragmentManager != null) {
      AstroHelper.launchAndroidDatePicker(fragmentManager, this);
    }
  }

  @Override
  public void onSubscriptionButtonClicked(String gender, String dob) {
    //Fire Analytics Event for subscribing.
    DialogAnalyticsHelper.deployAstroActionEvent(NhAnalyticsEventSection.NEWS, AstroTriggerAction
        .SUBSCRIBE.getTriggerAction(), DialogBoxType.ASTRO_ONBOARDING_PROMPT);
    String entityId =
        PreferenceManager.getPreference(AstroPreference.ASTRO_TOPIC_ID, Constants.EMPTY_STRING);
    AstroHelper.doAstroPostRequest(this, gender, dob, entityId);
  }

  @Override
  public void onAstroCrossButtonClicked() {
    DialogAnalyticsHelper.deployAstroActionEvent(NhAnalyticsEventSection.NEWS, AstroTriggerAction
        .CROSS_DISMISS.getTriggerAction(), DialogBoxType.ASTRO_ONBOARDING_PROMPT);
  }

  @Override
  public void onAstroEditButtonClicked() {
    DialogAnalyticsHelper.deployAstroViewedEvent(NhAnalyticsEventSection.NEWS,
        DialogBoxType.ASTRO_ONBOARDING_PROMPT);
  }

  @Override
  public void onGenderSelected(String genderStr) {
    Gender gender = Gender.getGender(genderStr);
    if (gender == null) {
      return;
    }
    setBackgroundToGenderView(gender);
    selectedGender = gender;
    AstroHelper.saveGender(genderStr);
    if (AstroHelper.canEnableSubscribeButton()) {
      subscribeButton.setEnabled(true);
      subscribeButton.setTextColor(ThemeUtils.getThemeColorByAttribute(getContext(),R.attr.app_rate_submit_text_color));
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      handleDialogDismiss();
    }
    return false;
  }

  public void showProgressBar() {
    astroProgressBarContainer.setVisibility(View.VISIBLE);
    enableOrDisableViews(false);
  }

  public void hideProgressBar() {
    astroProgressBarContainer.setVisibility(View.GONE);
    enableOrDisableViews(true);
  }

  private void enableOrDisableViews(boolean enable) {
    astroMaleText.setEnabled(enable);
    astroFemaleText.setEnabled(enable);
    astroDateValue.setEnabled(enable);
  }
}
