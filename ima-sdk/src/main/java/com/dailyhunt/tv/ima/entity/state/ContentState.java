package com.dailyhunt.tv.ima.entity.state;

/**
 * Generic Content State , need to be implemented by any type of content
 * As of now , it is used by VIDEO and AD State
 */

public interface ContentState {

  /**
   * Call back giving the index for the a content state
   *
   * @return -- index of the state
   */
  int getStateIndex();

  /**
   * Call back giving the content type
   *
   * @return -- content type
   */
  ContentType getContentType();

}
