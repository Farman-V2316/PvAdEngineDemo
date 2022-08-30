package com.newshunt.appview.common.ui.viewholder;

/* Copyright (c) 2016 Newshunt. All rights reserved.
 */

import android.view.View;

import com.newshunt.appview.R;
import com.newshunt.appview.common.ui.listeners.AddLocationListener;
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener;
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.asset.Location;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


/**
 * ViewHolder for showing city view inside Locations.
 *
 * @author  priya.gupta on 15/10/2020
 */
public class SuggestedLocationInfoViewHolder extends RecyclerView.ViewHolder implements
    View.OnClickListener {

  private final RecyclerViewOnItemClickListener viewOnItemClickListener;
  private final NHTextView locationCityTitle;
  private Location location;
  private AddLocationListener addLocationListener;
  private PageReferrer pageReferrer;
  private NhAnalyticsEventSection eventSection;
  private final ConstraintLayout suggestedLocationContainer;
  private LocationFollowClickListener locationFollowClickListener;


  public SuggestedLocationInfoViewHolder(final View itemView,
                                         final AddLocationListener addLocationListener,
                                         final RecyclerViewOnItemClickListener viewOnItemClickListener,
                                         final boolean showFollowButton,
                                         final PageReferrer pageReferrer,
                                         NhAnalyticsEventSection eventSection,
                                         final LocationFollowClickListener locationFollowClickListener) {
    super(itemView);

    this.locationCityTitle = itemView.findViewById(R.id.suggested_location_city_title);
    this.viewOnItemClickListener = viewOnItemClickListener;
    this.addLocationListener = addLocationListener;
    this.pageReferrer = pageReferrer;
    this.suggestedLocationContainer =
        itemView.findViewById(R.id.suggested_location_viewholder_container);
    this.locationFollowClickListener = locationFollowClickListener;
    itemView.setOnClickListener(this);
  }

  public void updateLocation(Location location) {
    if (location == null) {
      return;
    }
    this.location = location;
    ;
    this.locationCityTitle.setText(location.getDisplayName());
    suggestedLocationContainer.setSelected(location.isFollowed());
    setClickListeners();

  }

  private void setClickListeners() {
    if (location == null) {
      return;
    }

  }

  private void onTabAddedOrRemoved(boolean isAdded) {
    suggestedLocationContainer.setSelected(isAdded);
    if (addLocationListener != null) {
      addLocationListener.onLocationAdded(isAdded, location);
    }
    locationFollowClickListener.followed(isAdded, location);
    location.setFollowed(isAdded);

  }

  @Override
  public void onClick(View v) {
    if (v == suggestedLocationContainer ) {
      onTabAddedOrRemoved(!suggestedLocationContainer.isSelected());
    }

  }


}