package com.newshunt.analytics.helper;

import androidx.annotation.NonNull;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;

public interface CurrentReferrerProvider {
  PageReferrer getCurrentPageReferrer();

  PageReferrer getCurrentReferrerLead();

  PageReferrer getCurrentReferrerFlow();

  String getCurrentReferrerRaw();

  void updateCurrentReferrer(@NonNull PageReferrer referrer);
}
