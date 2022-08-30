/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.listener;

/**
 * This is used to communicate the response of the Astro Subscription response to the Astro dialog.
 * Created by anshul on 17/2/17.
 */

public interface AstroSubscriptionView {

  void onAstroSubscriptionSuccess();

  void onAstroSubscriptionFailed(String failureReason);

  void onDatePickerClicked();

  void onSubscriptionButtonClicked(String gender, String dob);

  void onAstroCrossButtonClicked();

  void onAstroEditButtonClicked();

  void onGenderSelected(String gender);

  void hideProgressBar();

  void showProgressBar();
}
