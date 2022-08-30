package com.dailyhunt.tv.players.player;


/**
 * Exo Player to play Video Content
 *
 * @author ranjith
 */

/*
public class ExoPlayerView extends RelativeLayout
    implements OnPreparedListener, OnCompletionListener, OnErrorListener,
    VideoControlsFullScreenListener, VideoControlsVisibilityListener,
    VideoPlayer, OnSeekCompletionListener, VideoControlsPlayPauseListener,
    VideoControlsSoundListener, OnBufferUpdateListener, VideoControlsSeekListener,
    VideoTimeListener, DhAudioFocusManagerInterface {

  //private final static String TAG = ExoPlayerView.class.getSimpleName();
  private TVExoplayerView emVideoView;
  private ImageView qualitySettingsIcon;
  private ProgressBar progressBar;
  private Disposable disposable;
  //Call backs for Content State + Player state ..
  private ContentStateProvider stateProvider;
  private VideoPlayerCallBack playerCallBack;
  private int curSeekPosition = 0;
  private boolean videoPausedBeforeInitialize;
  private boolean videoPausedByAdEvent;
  private ExoMediaPlayer mediaPlayer;
  private ExoMediaDelegate exoMediaDelegate;

  //Input Data ..
  private String dataUrl;
  private VideoQualitySettings qualitySettings;
  private boolean isEnableQualitySetting = false;
  private boolean live, muteMode, released, isCompleted, pauseState, addClientInfo, intercept;
  private boolean applyBufferSettings;
  private ImaAdsLoader adsLoader;
  private DhAudioManager audioManager = new DhAudioManager(this);

  public ExoPlayerView(final Context context) {
    super(context);
  }

  public ExoPlayerView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public ExoPlayerView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private void initView() {
    IMALogger.d(TAG, "INIT VIEW");

    emVideoView = getRootView().findViewById(R.id.exo_player);
    qualitySettingsIcon = getRootView().findViewById(R.id.exo_quality_change);
    progressBar = getRootView().findViewById(R.id.exo_progressbar);

    qualitySettingsIcon.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        onQualityChangeViewClick();
      }
    });

    exoMediaDelegate = new ExoMediaDelegate();
    exoMediaDelegate.setOnCompletionListener(this);
    exoMediaDelegate.setPreparedListener(this);
    exoMediaDelegate.setVideoControlsFullScreenListener(this);
    exoMediaDelegate.setPlayPauseListener(this);
    exoMediaDelegate.setSeekCompletionListener(this);
    exoMediaDelegate.setVideoControlsSoundListeners(this);
    exoMediaDelegate.setErrorListener(this);
    exoMediaDelegate.setBufferUpdateListener(this);
    exoMediaDelegate.setVideoTimeListener(this);
  }

  public String buildUrlWithClientInfo(String url) {
    Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
    uriBuilder.appendQueryParameter("clientId", ClientInfoHelper.getClientId());
    uriBuilder.appendQueryParameter("appLanguage", UserPreferenceUtil.getUserNavigationLanguage());
    uriBuilder.appendQueryParameter("langCode", UserPreferenceUtil.getUserLanguages());
    uriBuilder.appendQueryParameter("appVersion", ClientInfoHelper.getAppVersion());
    return uriBuilder.build().toString();
  }

  public void updateMuteMode(boolean muteMode) {
    this.muteMode = muteMode;
  }

  private void loadURIInPlayer() {
    IMALogger.d(TAG, "loadURIInPlayer");
    if (emVideoView == null) {
      return;
    }

    //for live set flag in view
    emVideoView.getController().setLive(live);
    emVideoView.getController().setMuteMode(muteMode);
    if (addClientInfo) {
      dataUrl = buildUrlWithClientInfo(dataUrl);
    }

    initBuilder(dataUrl);
  }

  private void initBuilder(String url) {
    if (CommonUtils.isEmpty(url)) {
      return;
    }

    VideoBuilder videoBuilder =
        new VideoBuilder(getContext(), isEnableQualitySetting ? qualitySettings : null,
            Uri.parse(url), true, live, muteMode, applyBufferSettings, adsLoader,
            emVideoView.getOverlayFrameLayout());
    if (mediaPlayer != null) {
      mediaPlayer.releasePlayer();
      mediaPlayer = null;
    }
    mediaPlayer = new ExoMediaPlayer(getContext(), videoBuilder, pauseState);
    mediaPlayer.initialisePlayer((pa)-> {
      PlayerAnalyticsHelper.logPA(pa);
      return Unit.INSTANCE;
    });
    mediaPlayer.setPlayerToView(emVideoView);
    mediaPlayer.setExoMediaDelegate(exoMediaDelegate);

    if (released) {
      mediaPlayer.clearResumePosition();
    }
    mediaPlayer.prepare();
    stateProvider.getVIDEOListener().onVideoPrepareInProgress();

    showOrHideThumbnail(true);
    IMALogger.d(TAG, "loadURIInPlayer :: qualitySettings :: " + qualitySettings);
    IMALogger.d(TAG, "loadURIInPlayer :: url :: " + url);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    IMALogger.d(TAG, "Finish Inflate");
    initView();
  }

  private void seekToSavedPosition() {
    if (curSeekPosition <= 0) {
      return;
    }
    mediaPlayer.seekTo(curSeekPosition);
    curSeekPosition = 0;
  }


  // ----------------- EXO Player Call backs ------------------------------//
  @Override
  public void onCompletion() {
    IMALogger.d(TAG, "Completion");
    stateProvider.getVIDEOListener().onVideoPlayComplete();

    curSeekPosition = 0; // As Video is complete ..
    if (mediaPlayer != null) {
      mediaPlayer.clearResumePosition();
    }
    onVideoCompleteOrError(true, mediaPlayer != null ? (int) mediaPlayer.duration() : -1);
  }

  @Override
  public void onLoopComplete() {
    //nothing here
  }


  @Override
  public void onPrepared() {
    IMALogger.d(TAG, "On Prepared");
    if (mediaPlayer == null) {
      IMALogger.d(TAG, "On Prepared :: mediaPlayer NULL");
      return;
    }

    stateProvider.getVIDEOListener().onVideoPrepared();

    //Remove THE Thumbnail with loader
    showOrHideThumbnail(false);

    if (emVideoView != null && playerCallBack != null && !playerCallBack.isCacheVideo()) {
      emVideoView.maybeShowController(true);
    }

    //Reset variables ..
    curSeekPosition = 0;
    isCompleted = false;
    mediaPlayer.startVideoTimer();

    if (playerCallBack != null) {
      playerCallBack.onPlayerReady();
    }

    if (playerCallBack != null && playerCallBack.isCacheVideo()) {
      IMALogger.d(TAG, "On Prepared - isCacheVideo :: true");
      mediaPlayer.pauseVideo();
      playerCallBack.onVideoPausedOnCaching();
    } else if (videoPausedBeforeInitialize || videoPausedByAdEvent) {
      IMALogger.d(TAG, "On Prepared - paused for videoPausedBeforeInitialize :: " +
          videoPausedBeforeInitialize + " :: videoPausedByAdEvent :: " + videoPausedByAdEvent);
      mediaPlayer.pauseVideo();
      videoPausedBeforeInitialize = false;
      videoPausedByAdEvent = false;
    } else if (playerCallBack != null) {
      IMALogger.d(TAG, "On Prepared - Video Started");
      //callback to fragment for video played beacon
      playerCallBack.onVideoStarted();
    }
  }

  @Override
  public void onFullScreenClicked() {
    onFullScreenViewClick();
  }

  @Override
  public void onControlsShown() {
    if (!isEnableQualitySetting) {
      qualitySettingsIcon.setVisibility(GONE);
      return;
    }
    qualitySettingsIcon.setVisibility(VISIBLE);
  }

  @Override
  public void onControlsHidden() {
    qualitySettingsIcon.setVisibility(GONE);
  }

  @Override
  public boolean onError(final ExoPlaybackException e) {
    IMALogger.d(TAG, "On Error " + e.getMessage());

    if (playerCallBack != null) {
      playerCallBack.logVideoError(e);
    }
    if (e.getCause() instanceof IllegalArgumentException) {
      if (playerCallBack != null) {
        playerCallBack.onVideoError(ExoPlaybackException.TYPE_UNEXPECTED);
      }
      if (mediaPlayer != null) {
        mediaPlayer.updateResumePosition();
      }
      pauseState = true;
      return false;
    }

    if (e.type == ExoPlaybackException.TYPE_SOURCE) {
      if (mediaPlayer != null) {
        mediaPlayer.updateResumePosition();
      }
      pauseState = true;
    }

    stateProvider.getVIDEOListener().onVideoError();
    onVideoCompleteOrError(false, -1);
    return true;
  }


  // ------------------------- Video Player Call backs ------------------------------ //

  @Override
  public void setInputData(final String dataUrl, final boolean isEnableQualitySetting,
                           final VideoQualitySettings settings, final boolean startPlay, final
                           boolean live, boolean muteMode, boolean addClientInfo,
                           boolean applyBufferSettings, ImaAdsLoader adsLoader) {
    IMALogger.d(TAG, "setInputData :: initialize Player");
    if (emVideoView == null) {
      return;
    }
    this.dataUrl = dataUrl;
    qualitySettings = settings;
    this.isEnableQualitySetting = isEnableQualitySetting;
    this.live = live;
    this.muteMode = muteMode;
    this.addClientInfo = addClientInfo;
    this.applyBufferSettings = applyBufferSettings;
    this.adsLoader = adsLoader;
    if (startPlay) {
      resumeVideoReq(false);
    } else {
      loadURIInPlayer();
    }
  }

  @Override
  public void resumeVideoReq(final boolean isAdEvent) {
    IMALogger.d(TAG, "Resume Video Req EXO");
    if (isAdEvent) {
      IMALogger.d(TAG, "videoPausedByAdEvent set to false");
      videoPausedByAdEvent = false;
    } else {
      IMALogger.d(TAG, "videoPausedBeforeInitialize set to false");
      videoPausedBeforeInitialize = false;
    }

    if (emVideoView == null) {
      IMALogger.d(TAG, "resumeVideoReq :: emVideoView NULL");
      return;
    }

    ContentState[] contentStates = stateProvider.getContentState();
    VideoState videoState = (VideoState) contentStates[0];

    IMALogger.d(TAG, "resumeVideoReq :: videoState :: " + videoState.name());


    switch (videoState) {
      case VIDEO_PREPARE_IN_PROGRESS: {
        if(mediaPlayer != null) {
          mediaPlayer.playVideoFromPrepareState();
        }
        break;
      }
      case VIDEO_PAUSED:
        if (playerCallBack != null && playerCallBack.isCacheVideo()) {
          // TODO to be confirmed whether to replay the video
          mediaPlayer.playVideo();
          stateProvider.getVIDEOListener().onVideoPlaying();
        } else {
          loadURIInPlayer();
        }
        break;

      case VIDEO_UNKNOWN:
      case VIDEO_ERROR:
      case VIDEO_QUALITY_CHANGE:
        loadURIInPlayer();
        break;
      case VIDEO_PREPARED:
        if (videoPausedBeforeInitialize) {
          IMALogger.d(TAG, "resumeVideoReq :: pauseVideo bcos of videoPausedBeforeInitialize");
          mediaPlayer.pauseVideo();
        } else if (videoPausedByAdEvent) {
          IMALogger.d(TAG, "resumeVideoReq :: pauseVideo bcos of videoPausedByAdEvent");
          mediaPlayer.pauseVideo();
        } else {
          IMALogger.d(TAG, "resumeVideoReq :: playVideo");
          mediaPlayer.playVideo();
          if (emVideoView != null) {
            emVideoView.maybeShowController(true);
          }
          if (playerCallBack != null) {
            IMALogger.d(TAG, "On Prepared - Video Started");
            //callback to fragment for video played beacon
            playerCallBack.onVideoStarted();
          }
        }
        break;
      case VIDEO_PLAYING:
        mediaPlayer.playVideo();
        break;
    }

    setVisibility(VISIBLE);   //Make the View Visible  ..
  }

  @Override
  public void pauseVideoReq(final boolean isAdEvent) {
    IMALogger.d(TAG, "Pause Video Req");
    if (isAdEvent) {
      IMALogger.d(TAG, "videoPausedByAdEvent");
      videoPausedByAdEvent = true;
    } else {
      IMALogger.d(TAG, "videoPausedBeforeInitialize");
      videoPausedBeforeInitialize = true;
    }

    if (emVideoView == null) {
      return;
    }

    ContentState[] contentStates = stateProvider.getContentState();
    VideoState videoState = (VideoState) contentStates[0];


    if (!ExoPlayerViewUtils.isVideoUnKnownOrError(videoState) && mediaPlayer.isPlaying()) {
      mediaPlayer.pauseVideo();
    }


    if (isAdEvent) { //Make View In Visible ..
      setVisibility(INVISIBLE);
    }
  }


  @Override
  public void pauseVideoWithReleaseReq(boolean isAdEvent) {

    if (isAdEvent) {
      IMALogger.d(TAG, "videoPausedByAdEvent");
      videoPausedByAdEvent = true;
    } else {
      IMALogger.d(TAG, "videoPausedBeforeInitialize");
      videoPausedBeforeInitialize = true;
    }


    if (emVideoView == null) {
      return;
    }

    ContentState[] contentStates = stateProvider.getContentState();
    VideoState videoState = (VideoState) contentStates[0];


    if (!ExoPlayerViewUtils.isVideoUnKnownOrError(videoState) && mediaPlayer.isPlaying()) {
      mediaPlayer.partialRelease();
    }

    if (isAdEvent) { //Make View In Visible ..
      setVisibility(INVISIBLE);
    }
  }

  @Override
  public int getVideoCurDuration() {
    if (mediaPlayer == null) {
      return 0;
    }

    ContentState[] contentStates = stateProvider.getContentState();
    VideoState videoState = (VideoState) contentStates[0];

    if (ExoPlayerViewUtils.isVideoUnKnownOrError(videoState)) {
      return 0;
    }

    return (int) mediaPlayer.getCurrentPosition();
  }

  @Override
  public int getVideoDuration() {
    if (mediaPlayer == null) {
      return 0;
    }

    ContentState[] contentStates = stateProvider.getContentState();
    VideoState videoState = (VideoState) contentStates[0];

    if (ExoPlayerViewUtils.isVideoUnKnownOrError(videoState)) {
      return 0;
    }

    return (int) mediaPlayer.duration();
  }

  @Override
  public void setVideoPlayerCallBack(final VideoPlayerCallBack callBack) {
    playerCallBack = callBack;
    if (playerCallBack != null) {
      emVideoView.getController().setControllerVisibilty(!playerCallBack.isHideController());
    }
  }

  @Override
  public void setContentStateProvider(final ContentStateProvider stateProvider) {
    this.stateProvider = stateProvider;
  }

  @Override
  public void releasePlayer() {
    IMALogger.d(TAG, "release Player");
    released = true;
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
    stateProvider.getVIDEOListener().onVideoUnknownState();
    playerCallBack = null;

    if (mediaPlayer != null) {
      mediaPlayer.releasePlayer();
      if (mediaPlayer != null) {
        // Release player is a blocking call so multiple threads executing this player can cause media player to be null here
        // TODO - check for synchronization
        mediaPlayer.clearResumePosition();
      }
    }
    emVideoView = null;
    mediaPlayer = null;
  }

  @Override
  public void showReplayButton() {
    if (emVideoView == null || emVideoView.getController() == null) {
      return;
    }
    emVideoView.getController().showReplayButton();
  }

  @Override
  public boolean isVideoComplete() {
    if (null == emVideoView) {
      return false;
    }

    ContentState[] contentStates = stateProvider.getContentState();
    VideoState videoState = (VideoState) contentStates[0];

    return videoState == VideoState.VIDEO_COMPLETE;

  }

  @Override
  public void onRestart() {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      IMALogger.d(TAG, "On Player Replay");
      stateProvider.getVIDEOListener().onVideoPlaying();
      if (playerCallBack != null) {
        playerCallBack.onVideoReplay();
      }
    }
  }

  // -------- Helper methods for the call backs to the VIEW holding this Player ----------------//

  private void showOrHideThumbnail(final boolean show) {
    if (playerCallBack == null) {
      return;
    }

    playerCallBack.showOrRemoveThumbnail(show);
  }

  private void onVideoCompleteOrError(final boolean isComplete, final int duration) {
    if (playerCallBack == null) {
      return;
    }

    if (isComplete) {
      emVideoView.getController().showReplayButton();
    }

    playerCallBack.onContentCompleteOrError(isComplete, duration);
  }

  private void onFullScreenViewClick() {
    if (playerCallBack == null) {
      return;
    }
    playerCallBack.onFullScreenClick();
  }

  private void onQualityChangeViewClick() {
    stateProvider.getVIDEOListener().onVideoQualityChange();
    mediaPlayer.pauseVideo();

    //Now call back to show the popup ..
    if (playerCallBack == null) {
      return;
    }

    playerCallBack.showVideoSettingsUI();
  }


  @Override
  public void onPlayerPlayPause() {
    if (mediaPlayer == null) {
      return;
    }

    //state provider for play/pause
    if (mediaPlayer.isPlaying()) {
      IMALogger.d(TAG, "On PlayerPlay");
      stateProvider.getVIDEOListener().onVideoPlaying();
      if (playerCallBack != null) {
        playerCallBack.onVideoResumed();
      }
    } else {
      IMALogger.d(TAG, "On PlayerPause");
      stateProvider.getVIDEOListener().onVideoPaused();
      if (playerCallBack != null) {
        playerCallBack.onVideoPaused();
      }
    }
  }

  @Override
  public void onSeekComplete() {
    if (null == mediaPlayer) {
      return;
    }

    //hack for full back video jumping to next video
    stateProvider.getContentState()[0] = VideoState.VIDEO_PLAYING;

    if (mediaPlayer.duration() == mediaPlayer.getCurrentPosition()) {
      //Video Dragged to End
      onCompletion();
    }
  }

  @Override
  public void setAudioToMute() {
    if (mediaPlayer != null) {
      mediaPlayer.setAudioToMute();
      audioManager.releaseAudioFocus();
    }
  }

  @Override
  public void setAudioToUnMute() {
    if (mediaPlayer != null) {
      mediaPlayer.setAudioToUnMute();
      audioManager.getAudioFocusRequest();
    }
  }

  @Override
  public boolean getAudioMuteState() {
    return mediaPlayer.getAudioMuteState();
  }

  public void getVideoUrlFromServer() {
    if (CommonUtils.isEmpty(dataUrl)) {
      return;
    }
    Single<ApiResponse<String>> observable =
        new InterceptUrlServiceImp(dataUrl, dataUrl).hitDataUrl();
    if (observable != null) {
      disposable = observable.subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              response -> {
                Logger.v(TAG, "onSuccess");
                try {
                  String result = decryptResponse(response.getData());
                  InterceptUrlResponse interceptUrlResponse =
                      JsonUtils.fromJson(result, InterceptUrlResponse.class);
                  initBuilder(interceptUrlResponse.getStreamUrl());
                } catch (Exception e) {
                  Logger.e(TAG, "exception: " + e.getMessage());
                  onVideoCompleteOrError(false, -1);
                }
              },
              e -> {
                Logger.v(TAG, "onError: " + e.getMessage());
                onVideoCompleteOrError(false, -1);
              });
    }
  }

  private String decryptResponse(String data) throws Exception {
    return ExoAESEncryption.decrypt(data);
  }

  public ContentStateProvider getContentStateProvider() {
    return stateProvider;
  }

  public void restart() {
    if (mediaPlayer == null) {
      return;
    }
    curSeekPosition = 0;
    mediaPlayer.restart();
    stateProvider.getVIDEOListener().onVideoPrepareInProgress();
  }

  @Override
  public void onBufferingUpdate(int percent) {
    IMALogger.d(TAG, "onBufferingUpdate : " + percent);
    if (percent < 100) {
      progressBar.setVisibility(View.VISIBLE);
    } else {
      progressBar.setVisibility(View.GONE);
    }
  }

  @Override
  public void onSeekStarted() {
    if (playerCallBack != null) {
      playerCallBack.onSeekStart();
    }
  }

  @Override
  public void onSeekEnded(long seekTime) {
    if (playerCallBack != null) {
      playerCallBack.onSeekComplete();
    }
  }

  @Override
  public void onTimeUpdate(String time, long position) {
    if (playerCallBack != null) {
      playerCallBack.onTimeUpdate(position);
    }
  }

  @Override
  public void showTimeLeft(boolean isShowRemaingTime) {

  }

  public boolean isVideoPlaying() {
    if (mediaPlayer != null) {
      return mediaPlayer.isPlaying();
    }
    return false;
  }

  public boolean isVideoOnError() {
    if (stateProvider == null || stateProvider.getVIDEOListener() == null) {
      return false;
    }
    return stateProvider.getVIDEOListener().getVideoState() == VideoState.VIDEO_ERROR;
  }

  @Override
  public void onAudioFocusGained() {
    if (mediaPlayer != null) {
      Logger.i(TAG , "onAudioFocusGained in Exoplayerview");
      mediaPlayer.setAudioToUnMute();
      if (playerCallBack != null) {
        playerCallBack.updatePlayerController(false);
      }
    }
  }

  @Override
  public void onAudioFocusLost() {
    if (playerCallBack != null && playerCallBack.isMediaPlayerVisible() && mediaPlayer != null) {
      Logger.i(TAG , "onAudioFocusLost in Exoplayerview");
      mediaPlayer.setAudioToMute();
      playerCallBack.updatePlayerController(true);
    }
  }

  @Override
  public void onAudioFocusRequestGranted() {

  }

  @Override
  public void onAudioFocusLostTransient() {

  }
}
*/
