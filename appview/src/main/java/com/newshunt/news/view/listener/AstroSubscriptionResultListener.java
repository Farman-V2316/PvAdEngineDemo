/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.listener;

/**
 * Interface for notifying the NewsHomeActivity about the success or failure of the Astro
 * subscription
 * Created by anshul on 20/2/17.
 */

public interface AstroSubscriptionResultListener {

  void onAstroSubscriptionSuccess();

  void onAstroSubscriptionFailed(String failureReason);

}
