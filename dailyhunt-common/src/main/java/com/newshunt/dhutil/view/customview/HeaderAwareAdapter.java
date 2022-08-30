package com.newshunt.dhutil.view.customview;

/**
 * Interface for adapters to get them their actual positions incase they are using headers
 *
 * @author maruti.borker
 */
public interface HeaderAwareAdapter {
  int getActualPosition(int oldPosition);
}
