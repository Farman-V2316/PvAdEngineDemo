package com.dailyhunt.tv.players.fragment.base;

import android.os.Bundle;

import com.dailyhunt.tv.players.constants.PlayerContants;
import com.dailyhunt.tv.players.interfaces.PlayerCallbacks;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.common.view.view.BaseFragment;

/**
 * Created by Jayanth on 09/05/18.
 */

public class BasePlayerFragment extends BaseFragment {

  protected PlayerCallbacks playerCallbacks;

  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);

    //TODO::Vinod.bc - Comment this for Demo, need to check on Fragment recreation issue
//    if (tvPlayerCallbacks == null) {
//      throw new IllegalArgumentException(getString(R.string.err_msg_player_callback_null));
//    }

//    Bundle bundle = getArguments();
//    if (bundle == null) {
//      throw new IllegalArgumentException(getString(R.string.err_msg_player_bundle_null));
//    }

  }

  public PlayerCallbacks getPlayerCallbacks() {
    return playerCallbacks;
  }

  public void setPlayerCallbacks(PlayerCallbacks playerCallbacks) {
    this.playerCallbacks = playerCallbacks;
  }

  public NhAnalyticsEventSection getSection(Bundle bundle) {
    NhAnalyticsEventSection section = NhAnalyticsEventSection.TV;
    if (bundle != null) {
      section = (NhAnalyticsEventSection) bundle.getSerializable(PlayerContants.BUNDLE_SECTION);
    }
    return section;
  }
}
