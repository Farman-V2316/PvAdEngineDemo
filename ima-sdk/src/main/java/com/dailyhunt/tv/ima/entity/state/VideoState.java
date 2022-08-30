package com.dailyhunt.tv.ima.entity.state;

/**
 * Different States that can be possible for an Video
 * This state is managed or updated with help of Video player
 * <p>
 * Player is requested to take corresponding states based on the states.
 *
 * @author ranjith
 */

public enum VideoState implements ContentState {

  VIDEO_UNKNOWN(0),               // We don't know the current state [i.e it is not either 0 -- 5]
  VIDEO_QUALITY_CHANGE(1),        // When Quality change is req.
  VIDEO_PREPARE_IN_PROGRESS(2),  // When a Video is in preparing state ..
  VIDEO_PREPARED(3),            // When a Video is prepared ..
  VIDEO_PLAYING(4),             // When a Video is playing ..
  VIDEO_PAUSED(5),              // When a Video is paused ..
  VIDEO_COMPLETE(6),            // When a Video is Completed ..
  VIDEO_ERROR(7);               // When a Video Error occurred..

  private int index;

  VideoState(int index) {
    this.index = index;
  }

  @Override
  public int getStateIndex() {
    return index;
  }

  @Override
  public ContentType getContentType() {
    return ContentType.VIDEO;
  }
}
