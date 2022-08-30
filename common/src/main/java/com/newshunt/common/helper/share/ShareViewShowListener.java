/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share;

import android.content.Intent;

/**
 * Listener to get callback for share click.
 *
 * @author sumedh.tambat.
 */
public interface ShareViewShowListener {

  void onShareViewClick(String packageName, ShareUi shareUi);

  Intent getIntentOnShareClicked(ShareUi shareUi);
}
