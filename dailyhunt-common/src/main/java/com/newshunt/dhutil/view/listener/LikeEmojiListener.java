/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.listener;

import com.newshunt.dataentity.social.entity.LikeType;

/**
 * Like/Unlike Listener
 *
 * @author umesh.isran
 */
public interface LikeEmojiListener {
  void onLikeEmojiSelected(LikeType likeType);

  void onUnlikeSelected(LikeType likeType);
}
