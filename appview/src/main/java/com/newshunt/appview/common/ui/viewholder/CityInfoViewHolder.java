/* Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.newshunt.appview.R;
import com.newshunt.appview.common.ui.listeners.AddLocationListener;
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener;
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener;
import com.newshunt.common.view.customview.NHImageView;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.asset.Location;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder for showing city view inside Locations.
 *
 * @author priya.gupta on 15/10/2020
 */
public class CityInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
  private final TextView cityNameTv;

  private final RecyclerViewOnItemClickListener viewOnItemClickListener;
  private final View parent;
  private Location location;
  private FrameLayout isLocationFavoriteContainer;
  private final NHImageView isLocationFavorite;
  private AddLocationListener addLocationListener;
  private LocationFollowClickListener locationFollowClickListener;

  private PageReferrer pageReferrer;
  private NhAnalyticsEventSection eventSection;

  public CityInfoViewHolder(final View itemView,
                            final AddLocationListener addLocationListener,
                            final RecyclerViewOnItemClickListener viewOnItemClickListener,
                            final PageReferrer pageReferrer,
                            NhAnalyticsEventSection eventSection,
                            final LocationFollowClickListener locationFollowClickListener) {
    super(itemView);
    cityNameTv = (NHTextView) itemView.findViewById(R.id.location_city_title);

    parent = itemView.findViewById(R.id.location_list_child_view_parent);
    isLocationFavoriteContainer = itemView.findViewById(R.id.location_isfavorite_container);
    this.viewOnItemClickListener = viewOnItemClickListener;
    this.addLocationListener = addLocationListener;
    this.pageReferrer = pageReferrer;
    isLocationFavorite = itemView.findViewById(R.id.location_isfavorite);
    isLocationFavoriteContainer.setVisibility(View.VISIBLE);
    isLocationFavoriteContainer.setOnClickListener(this);
    this.eventSection = eventSection;
    this.locationFollowClickListener = locationFollowClickListener;
  }

  public void updateLocation(Location location) {
    if (location == null) {
      return;
    }
    this.location = location;
    cityNameTv.setText((location.getName()));
    isLocationFavorite.setSelected(location.isFollowed());
    cityNameTv.setSelected(location.isFollowed());
    setClickListeners();
  }

  private void setClickListeners() {
    if (location == null) {
      return;
    }
  }

  private void onTabAddedOrRemoved(boolean isAdded) {
    isLocationFavorite.setSelected(isAdded);
    cityNameTv.setSelected(isAdded);
    if (addLocationListener != null) {
      addLocationListener.onLocationAdded(isAdded, location);
    }
    locationFollowClickListener.followed(isAdded, location);
    location.setFollowed(isAdded);
  }

  @Override
  public void onClick(View v) {
    if (v == isLocationFavorite || v == isLocationFavoriteContainer) {
      onTabAddedOrRemoved(!isLocationFavorite.isSelected());
    }
  }


}