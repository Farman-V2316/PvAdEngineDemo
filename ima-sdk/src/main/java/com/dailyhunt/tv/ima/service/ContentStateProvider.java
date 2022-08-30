package com.dailyhunt.tv.ima.service;

import com.dailyhunt.tv.ima.entity.state.ContentState;

/**
 * Interface to be implemented by Content State Provider to give the content state
 *
 * @author ranjith
 */

public interface ContentStateProvider {

  /**
   * Method to give the content State , which is a two dimensional Array
   * <p>
   * Index 0 -- Video State
   * Index 1 -- AD State
   *
   * @return -- Array of Content State based on above format ..
   */
  ContentState[] getContentState();

  /**
   * Method for getting current in use AdState Listener
   *
   * @return -- AdStateListenerService Object
   */
  AdStateListenerService getADListener();

  /**
   * Method for getting current in use VideoState Listener
   *
   * @return -- VideoStateListenerService Object
   */
  VideoStateListenerService getVIDEOListener();

}
