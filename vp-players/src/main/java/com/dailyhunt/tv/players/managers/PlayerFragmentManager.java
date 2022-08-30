package com.dailyhunt.tv.players.managers;

import android.os.Bundle;
import android.view.View;

import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction;
import com.dailyhunt.tv.players.constants.PlayerContants;
import com.dailyhunt.tv.players.fragment.PlayerFragmentYoutube;
import com.dailyhunt.tv.players.fragment.base.BasePlayerFragment;
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks;
import com.dailyhunt.tv.players.interfaces.PlayerViewDH;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * Singleton Player Manger responsible for showing differnet types of players possible in app
 * Created by Jayanth on 09/05/18.
 */

public class PlayerFragmentManager {

  private static final String TAG = PlayerFragmentManager.class.getSimpleName();
  private static final String TV_FRAGMENT_TAG = "TV_FRAGMENT_TAG";

  private PlayerViewDH tvPlayerFragment;

  /**
   * To avoid memory leaks in NewsDetailFragment
   * @param item
   * @param holder
   * @param parentFrg
   * @param playerCallbacks
   * @param pageReferrer
   * @param section
   */
  public PlayerFragmentManager(PlayerAsset item, final View holder, Fragment parentFrg,
                               PlayerCallbacks playerCallbacks,
                               PageReferrer pageReferrer, NhAnalyticsEventSection section) {
    FragmentTransaction transaction = parentFrg.getChildFragmentManager().beginTransaction();
    removePreviousFragment(transaction);

    Bundle bundle = new Bundle();
    BasePlayerFragment newFragment = null;
    if (item == null || item.getType() == null || item.getType() != PlayerType.YOUTUBE) {
      return;
    }

    newFragment = new PlayerFragmentYoutube();
    newFragment.setPlayerCallbacks(playerCallbacks);
    if (item instanceof ExoPlayerAsset) {
      bundle.putSerializable(PlayerContants.EXO_PLAYER_ASSET_ITEM, item);
    } else {
      bundle.putSerializable(PlayerContants.PLAYER_ASSET_ITEM, item);
    }
    bundle.putSerializable(PlayerContants.PLAYER_ASSET_ITEM, item);
    bundle.putSerializable(PlayerContants.BUNDLE_FRAGMENT_REFERRER, pageReferrer);
    bundle.putSerializable(PlayerContants.BUNDLE_SECTION, section);
    newFragment.setArguments(bundle);
    tvPlayerFragment = (PlayerViewDH) newFragment;
    if (tvPlayerFragment != null) {
      tvPlayerFragment.setStartAction(PlayerVideoStartAction.CLICK);
    }
    try {
      transaction.replace(holder.getId(), newFragment, TV_FRAGMENT_TAG);
      transaction.addToBackStack(null);
      transaction.commitAllowingStateLoss();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public PlayerViewDH getPlayerFragment() {
    return tvPlayerFragment;
  }

  private void removePreviousFragment(FragmentTransaction transaction) {
    if (tvPlayerFragment != null) {
      tvPlayerFragment.releasePlayer();
      transaction.remove((Fragment) tvPlayerFragment);
      tvPlayerFragment = null;
    }
  }

  public static void removeChildFragments(Fragment fragment) {
    while (fragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
      Logger.d("PlayerFragmentManager", "removeChildFragments ->");
      fragment.getChildFragmentManager().popBackStackImmediate();
    }
  }

}
