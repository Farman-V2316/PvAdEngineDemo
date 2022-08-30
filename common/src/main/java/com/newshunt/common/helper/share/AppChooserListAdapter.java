/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newshunt.common.util.R;
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener;

import java.util.List;


/**
 * Adapter to set a app name and it's image.
 *
 * @author sumedh.tambat
 */
public class AppChooserListAdapter
    extends RecyclerView.Adapter<AppChooserListAdapter.AppChooserViewHolder> {

  private List<ShareAppDetails> appDetails;
  private Context context;
  private RecyclerViewOnItemClickListener viewOnItemClickListener;

  public AppChooserListAdapter(Context context, List<ShareAppDetails> appDetails,
                               RecyclerViewOnItemClickListener viewOnItemClickListener) {

    this.appDetails = appDetails;
    this.context = context;
    this.viewOnItemClickListener = viewOnItemClickListener;
  }

  @Override
  public AppChooserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new AppChooserViewHolder(createNewsView(parent));
  }

  private View createNewsView(ViewGroup parent) {
    return LayoutInflater.from(parent.getContext())
        .inflate(R.layout.app_chooser_options, parent, false);
  }

  @Override
  public void onBindViewHolder(AppChooserViewHolder holder,
                               int position) {
    holder.appName.setText(getContentItem(position).getAppName());
    holder.appImage.setImageDrawable(getContentItem(position).getAppIcon());
  }

  @Override
  public int getItemCount() {
    return appDetails.size();
  }

  public ShareAppDetails getContentItem(int position) {
    return appDetails.get(position);
  }

  public List<ShareAppDetails> getStories() {
    return appDetails;
  }

  /**
   * Hold views for a chooser view.
   */
  public class AppChooserViewHolder extends RecyclerView.ViewHolder {
    public ImageView appImage;
    public TextView appName;

    public AppChooserViewHolder(View view) {
      super(view);

      appImage = (ImageView) view.findViewById(R.id.application_image);
      appName = (TextView) view.findViewById(R.id.application_name);

      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          viewOnItemClickListener.onItemClick(new Intent(), getPosition());
        }
      });
    }
  }
}
