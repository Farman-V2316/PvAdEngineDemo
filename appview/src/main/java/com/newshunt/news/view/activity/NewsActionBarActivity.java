/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.activity;

import android.graphics.Typeface;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newshunt.dhutil.view.TopBarNavigationView;
import com.newshunt.appview.R;


/**
 * Contains common action bar implementation.
 *
 * @author nilesh.borkar
 */
/*
public class NewsActionBarActivity extends NewsListDetailActivity {

  private DrawerLayout drawerLayout;
  private TextView actionbarTitleText;
  private ImageView actionbarBackButton;
  private ImageView dotIcon;
  private Toolbar actionBar;
  private FrameLayout.LayoutParams paramsWithMarginTop;
  private FrameLayout.LayoutParams paramsWithNoMarginTop;

  @Override
  public void setContentView(int layoutResID) {
    super.setContentView(R.layout.activity_wrapper);

    RelativeLayout childContainer = (RelativeLayout) findViewById(R.id.child_container);
    LayoutInflater.from(this).inflate(layoutResID, childContainer);
    setupCustomActionBar();
  }

  private void setupCustomActionBar() {
    actionBar = (Toolbar) findViewById(R.id.actionbar);
    setSupportActionBar(actionBar);

    actionbarTitleText = (TextView) actionBar.findViewById(R.id.actionbar_title);
    actionbarTitleText.setTypeface(null, Typeface.BOLD);

    LinearLayout backButtonLayout = (LinearLayout) findViewById(R.id.actionbar_back_button_layout);

    backButtonLayout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
	  // TODO(refactor)
        */
/*if(!(view.getContext() instanceof NewsHomeActivity)) {
          NewsActionBarActivity.this.onBackPressed();
        }*//*

      }
    });

    actionbarBackButton = (ImageView) actionBar.findViewById(R.id.actionbar_back_button);
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawerLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    paramsWithMarginTop = (FrameLayout.LayoutParams) drawerLayout.getLayoutParams();

    paramsWithNoMarginTop = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT);
    paramsWithNoMarginTop.setMargins(0, 0, 0, 0);
  }

  public TextView getActionbarTitleText() {
    return actionbarTitleText;
  }


  public ImageView getDotIcon() {
    if (dotIcon == null) {
      dotIcon = (ImageView) findViewById(R.id.dot_icon);
    }
    return dotIcon;
  }

  public ImageView getActionbarBackButton() {
    return actionbarBackButton;
  }

  public Toolbar getCustomActionBar() {
    return actionBar;
  }

  public DrawerLayout getDrawerLayout() {
    return drawerLayout;
  }

  public void setToolbarOptionContainer() {
    actionbarTitleText.setVisibility(View.GONE);
    LinearLayout toolbarOptionContainer =
        (LinearLayout) actionBar.findViewById(R.id.toolbar_option_container);
    TopBarNavigationView navigationView = new TopBarNavigationView(NewsActionBarActivity.this);
    toolbarOptionContainer.addView(navigationView);
  }

  protected void setDrawerLayoutMargin() {
    drawerLayout.setLayoutParams(paramsWithMarginTop);
  }

  protected void removeDrawerLayoutMargin() {
    drawerLayout.setLayoutParams(paramsWithNoMarginTop);
  }

  public void disableNavigationDrawer() {
    getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
  }

  protected void setTitleOnActionBar(String pTitle) {
    findViewById(R.id.actionbar_back_button_layout).setVisibility(View.GONE);
    actionbarBackButton.setVisibility(View.GONE);
    actionbarTitleText.setText(pTitle);
    actionbarTitleText.setVisibility(View.VISIBLE);
  }
}*/
