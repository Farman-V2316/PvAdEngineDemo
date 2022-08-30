package com.dailyhunt.tv.ima.playerholder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dailyhunt.tv.ima.IMALogger;
import com.dailyhunt.tv.ima.R;
import com.dailyhunt.tv.ima.protocol.AdPlayerProtocol;
import com.dailyhunt.tv.ima.protocol.ContentPlayerProtocol;
import com.dailyhunt.tv.ima.protocol.VideoPlayerProtocol;

/**
 * Content Player Holder,  that is responsible for playing the Content [Video + AD] by IMA SDK
 * <p>
 * The Holder interacts with Video and Ad holder accordingly with help of IMA call backs..
 *
 * @author ranjith
 */

public class ContentPlayerHolder extends FrameLayout implements ContentPlayerProtocol {

  private final static String TAG = ContentPlayerHolder.class.getSimpleName();

  private AdPlayerProtocol adPlayProtocol;
  private VideoPlayerProtocol videoPlayProtocol;
  private ProgressBar progressBar;

  public ContentPlayerHolder(Context context) {
    super(context);
  }

  public ContentPlayerHolder(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ContentPlayerHolder(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    IMALogger.d(TAG, "ON Finish Inflate");
    initialize();
  }

  private void initialize() {
    videoPlayProtocol = (VideoPlayerProtocol) getRootView().findViewById(R.id.video_player_holder);
    videoPlayProtocol.initialize();

    adPlayProtocol = (AdPlayerProtocol) getRootView().findViewById(R.id.ad_player_holder);
    adPlayProtocol.initialize();

    progressBar = (ProgressBar) getRootView().findViewById(R.id.intermediate_progress);
  }

  @Override
  public AdPlayerProtocol getAdProtocol() {
    return adPlayProtocol;
  }

  @Override
  public VideoPlayerProtocol getVideoProtocol() {
    return videoPlayProtocol;
  }

  @Override
  public void showOrHideIntermediateProgress(boolean show) {
    if (show) {
      progressBar.setVisibility(VISIBLE);
    } else {
      progressBar.setVisibility(GONE);
    }
  }

  @Override
  public void setViewParams(RelativeLayout.LayoutParams params) {
    setLayoutParams(params);
  }

}
