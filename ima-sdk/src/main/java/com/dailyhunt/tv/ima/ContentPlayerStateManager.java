package com.dailyhunt.tv.ima;

import com.dailyhunt.tv.ima.callback.AdPlayerCallBack;
import com.dailyhunt.tv.ima.entity.model.ContentAdType;
import com.dailyhunt.tv.ima.entity.state.ContentState;
import com.dailyhunt.tv.ima.service.AdStateListenerService;
import com.dailyhunt.tv.ima.service.AdStateListenerServiceImpl;
import com.dailyhunt.tv.ima.service.ContentStateProvider;
import com.dailyhunt.tv.ima.service.VideoStateListenerService;
import com.dailyhunt.tv.ima.service.VideoStateListenerServiceImpl;

/**
 * Content Player State Manager for holding Video state and Ad State
 *
 * @author ranjith
 */

public class ContentPlayerStateManager implements ContentStateProvider {

  private AdStateListenerService adService;
  private VideoStateListenerService videoService;


  public ContentPlayerStateManager(AdPlayerCallBack adCallBack, ContentAdType adType) {
    if (adType != null) {
      adService = new AdStateListenerServiceImpl(adCallBack, adType);
    }
    videoService = new VideoStateListenerServiceImpl();
  }

  @Override
  public ContentState[] getContentState() {
    ContentState[] contentState;
    if (adService != null) {
      contentState = new ContentState[2];
      contentState[0] = videoService.getVideoState();
      contentState[1] = adService.getAdState();
    } else {
      contentState = new ContentState[1];
      contentState[0] = videoService.getVideoState();
    }
    return contentState;
  }

  @Override
  public AdStateListenerService getADListener() {
    return adService;
  }

  @Override
  public VideoStateListenerService getVIDEOListener() {
    return videoService;
  }
}
