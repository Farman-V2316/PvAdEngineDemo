/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.listener;

import android.content.Intent;

/**
 * Listener for single item clicked on recycler view.
 *
 * @author nilesh.borkar
 */
public interface RecyclerViewOnItemClickListener {
  void onItemClick(Intent intent, int position);
}