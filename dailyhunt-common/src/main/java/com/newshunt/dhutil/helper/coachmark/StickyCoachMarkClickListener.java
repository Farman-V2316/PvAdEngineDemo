package com.newshunt.dhutil.helper.coachmark;

import android.view.View;

/**
 * Listener for view click on sticky coach mark screen.
 *
 * @author datta.vitore.
 */
public interface StickyCoachMarkClickListener {

  void onStickyViewClick(View view, int id);

  void onStickyViewDismiss(int position);

  void hideDot();
}
