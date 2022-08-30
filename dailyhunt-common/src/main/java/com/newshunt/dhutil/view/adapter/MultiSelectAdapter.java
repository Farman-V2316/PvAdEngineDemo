/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.adapter;

import java.util.List;

/**
 * @author satosh.dhanyamraju
 */
public interface MultiSelectAdapter<T> {
  boolean isInActionMode();

  boolean isChecked(int position);

  void setIsActionMode(boolean actionMode);

  void toggleSelection(int pos);

  void clearSelections();

  int getSelectedItemCount();

  List<T> getSelectedItems();
}
