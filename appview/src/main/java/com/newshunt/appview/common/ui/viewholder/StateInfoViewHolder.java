/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.newshunt.appview.R;
import com.newshunt.appview.common.ui.adapter.CityListAdapter;
import com.newshunt.appview.common.ui.listeners.AddLocationListener;
import com.newshunt.appview.common.ui.listeners.StateLocationClickListener;
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.asset.Location;
import com.newshunt.dataentity.common.asset.Locations;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.view.EntityImageUtils;
import com.newshunt.helper.ImageUrlReplacer;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder for showing state view inside Locations
 *
 * @author priya.gupta on 15/10/2020.
 */
public class StateInfoViewHolder extends RecyclerView.ViewHolder {
  private TextView stateNameTv;
  private ImageView expandToggle;
  private ConstraintLayout expandToggleContainer;
  private Locations favouritableLocation;
  private RecyclerView childLocationList;
  private ImageView stateLogo;
  private View divider;
  private PageReferrer pageReferrer;
  private RecyclerViewOnItemClickListener viewOnItemClickListener;
  private AddLocationListener addLocationListener;
  private boolean showFollowButton;
  private NhAnalyticsEventSection eventSection;
  private ArrayList<String> expandedLocations;
  private StateLocationClickListener stateLocationClickListener;
  private CityListAdapter adapter;

  public StateInfoViewHolder(View itemView,
                             final StateLocationClickListener stateLocationClickListener,
                             final RecyclerViewOnItemClickListener viewOnItemClickListener,
                             final PageReferrer pageReferrer,
                             final AddLocationListener addLocationListener,
                             final boolean showFollowButton,
                             final NhAnalyticsEventSection eventSection,
                             ArrayList<String> expandedLocations) {
    super(itemView);

    this.stateLocationClickListener = stateLocationClickListener;
    stateNameTv = (TextView) itemView.findViewById(R.id.location_state_title);
    expandToggle = (ImageView) itemView.findViewById(R.id.expand_toggle);
    expandToggleContainer = itemView.findViewById(R.id.expand_toggle_container);
    childLocationList = itemView.findViewById(R.id.child_location_list);
    divider = itemView.findViewById(R.id.horizontal_view);
    stateLogo = itemView.findViewById(R.id.state_logo);
    this.pageReferrer = pageReferrer;
    this.viewOnItemClickListener = viewOnItemClickListener;
    this.addLocationListener = addLocationListener;
    this.showFollowButton = showFollowButton;
    this.eventSection = eventSection;
    this.expandedLocations = expandedLocations;

    expandToggleContainer.setOnClickListener(v -> {
      final Locations locationNode = favouritableLocation;
      if (stateLocationClickListener != null) {
        stateLocationClickListener.expandLocationList(locationNode.getParent());
      }
      if (locationNode.getAreChildrenVisible()) {
        onLocationCollapseClick(locationNode);
      } else {
        onLocationExpandClick(locationNode);
      }
    });

  }

  private void onLocationExpandClick(Locations locationNode) {
    if (CommonUtils.isEmpty(locationNode.getKids())) {
      return;
    }
    childLocationList.setVisibility(View.VISIBLE);
    divider.setVisibility(View.VISIBLE);
    childLocationList.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
    if(adapter== null) {
      adapter = new CityListAdapter(locationNode.getKids(),
          viewOnItemClickListener, pageReferrer, addLocationListener, eventSection);

      childLocationList.setAdapter(adapter);
    }else{
      adapter.setItems(locationNode.getKids());
    }

    expandToggle.setImageResource(R.drawable.arrow_collapse);
    locationNode.setAreChildrenVisible(true);
  }

  private void onLocationCollapseClick(Locations location) {
    expandToggle.setImageResource(R.drawable.arrow_expand);
    location.setAreChildrenVisible(false);
    childLocationList.setVisibility(View.GONE);
    divider.setVisibility(View.GONE);
  }

  public void updateStateItem(Locations location) {
    Location state = location.getParent();
    if (location == null) {
      return;
    }
    this.favouritableLocation = location;
    stateNameTv.setText((state.getName()));
    divider.setVisibility(View.GONE);
    expandToggle.setImageResource(location.getAreChildrenVisible() ? R.drawable.arrow_collapse : R
        .drawable.arrow_expand);
    String iconImageUrl = favouritableLocation.getParent().getCircleImageUrl();
    iconImageUrl =
        ImageUrlReplacer.getQualifiedImageUrl(iconImageUrl, CommonUtils.getDimension(R.dimen
            .alltopic_icon_w_h), CommonUtils.getDimension(R.dimen.alltopic_icon_w_h));
    EntityImageUtils.loadImage(iconImageUrl, favouritableLocation.getParent().getNameEnglish(),
        stateLogo);
    if (location.getAreChildrenVisible()) {
      onLocationExpandClick(location);
    }
    else if (expandedLocations.contains(location.getParent().getId())) {
      onLocationExpandClick(location);
    } else {
      onLocationCollapseClick(location);
    }
  }
}