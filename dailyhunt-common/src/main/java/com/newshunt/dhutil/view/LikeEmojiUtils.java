/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.newshunt.dataentity.news.model.entity.Counts;
import com.newshunt.dataentity.social.entity.LikeType;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.view.listener.LikeEmojiListener;

/**
 * @author umesh.isran
 */
public class LikeEmojiUtils {

  public static int getCircularEmojiResourceId(LikeType likeType, boolean show_all_white_icons) {
    if(likeType == null) {
      return show_all_white_icons ? R.drawable.ic_like_white :R.drawable.ic_like;
    }
    switch (likeType) {
      case LIKE:
        return R.drawable.circular_like_active;
      case LOVE:
        return R.drawable.circular_emoji_love;
      case HAPPY:
        return R.drawable.circular_emoji_haha;
      case SAD:
        return R.drawable.circular_emoji_sad;
      case WOW:
        return R.drawable.circular_emoji_wow;
      case ANGRY:
        return R.drawable.circular_emoji_angry;
      default:
        return R.drawable.circular_like_active;
    }
  }
}