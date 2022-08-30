<!DOCTYPE html>
<html>

<body>
    <div style="position: relative; display: block; max-width: 100%;">
        <div style="padding-top: 168.388%;">
            <video id="myPlayerID" data-video-id="DH_PLAYER_VIDEO_ID" data-account="5384493731001" data-player="B13DWLmhZ" data-embed="default" data-application-id class="video-js" controls style="position: absolute; top: 0px; right: 0px; bottom: 0px; left: 0px; width: 100%; height: 100%;">
        </video>
            <script src="http://players.brightcove.net/5384493731001/B13DWLmhZ_default/index.min.js"></script>


            <script type="text/javascript">
                var myPlayer;
                var callBackNativeMapping = new Object();
                var pauseVideo = false;
                var isPauseExecuted = false;

                videojs("myPlayerID").ready(function() {
                    myPlayer = this;
                    WebPlayerInterface.showRemainingTime();
                    WebPlayerInterface.onReady();
                    WebPlayerInterface.log('on Ready');

                    videojs("myPlayerID").on('ended', function() {
                        WebPlayerInterface.log('video ended');
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['ended'], -1);
                    })

                    videojs("myPlayerID").on('pause', function() {
                        WebPlayerInterface.log('video paused');
                        isPauseExecuted = true;
                        pauseVideo = false;
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['pause'], -1);
                        if (myPlayer != null) {
                            WebPlayerInterface.getMuteMode(myPlayer.muted());
                        }
                    })

                    videojs("myPlayerID").on('error', function() {
                        WebPlayerInterface.log('error playing');
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['error'], -1);
                        if (myPlayer != null) {
                            WebPlayerInterface.getMuteMode(myPlayer.muted());
                        }
                    })

                    myPlayer.on('volumechange', function(evt) {
                        WebPlayerInterface.log('volumechange');
                        if (myPlayer != null) {
                            WebPlayerInterface.getMuteMode(myPlayer.muted());
                        }
                    })
                });

                videojs('myPlayerID').on('loadedmetadata', function() {
                    myPlayer = this;
                    myPlayer.on('ads-request', function(evt) {
                        WebPlayerInterface.log('ads-ad-started event passed to event handler');
                    })

                    myPlayer.on('ads-load', function(evt) {
                        WebPlayerInterface.log('ads-load event passed to event handler');
                    })

                    myPlayer.on('ads-pod-started', function(evt) {
                        WebPlayerInterface.log('ads-pod-started event passed to event handler');
                    })

                    myPlayer.on('ads-pod-ended', function(evt) {
                        WebPlayerInterface.log('ads-pod-ended event passed to event handler');
                    })

                    myPlayer.on('ads-allpods-completed', function(evt) {
                        WebPlayerInterface.log('ads-allpods-completed event passed to event handler');
                    })

                    myPlayer.on('ads-ad-started', function(evt) {
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['ads-ad-started'], -1);
                        WebPlayerInterface.log('ads-ad-started event passed to event handler');
                    })

                    myPlayer.on('ads-ad-ended', function(evt) {
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['ads-ad-ended'], -1);
                        WebPlayerInterface.log('ads-ad-ended event passed to event handler');
                    })

                    myPlayer.on('ads-ad-skipped', function(evt) {
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['ads-ad-skipped'], -1);
                        WebPlayerInterface.log('ads-ad-skipped event passed to event handler');
                    })

                    myPlayer.on('ads-pause', function(evt) {
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['ads-pause'], -1);
                        WebPlayerInterface.log('ads-pause event passed to event handler');
                    })

                    myPlayer.on('ads-play', function(evt) {
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['ads-play'], -1);
                        WebPlayerInterface.log('ads-play event passed to event handler');
                    })

                    myPlayer.on('ads-click', function(evt) {
                        WebPlayerInterface.log('ads-click event passed to event handler');
                    })

                    myPlayer.on('play', function(evt) {
                        WebPlayerInterface.log('play started');
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['play'], -1);
                    })

                    myPlayer.on('playing', function(evt) {
                        WebPlayerInterface.log('playing');
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['playing'], -1);
                        if (myPlayer != null) {
                            WebPlayerInterface.getMuteMode(myPlayer.muted());
                        }
                    })

                    myPlayer.on('timeupdate', function(evt) {
                        WebPlayerInterface.log('timeupdate');
                        if (pauseVideo && !isPauseExecuted) {
                            WebPlayerInterface.log('timeupdate::inside pauseVideo');
                            if (myPlayer != null) {
                                myPlayer.pause();
                            }
                            pauseVideo = false;
                        }
                        isPauseExecuted = false;
                        WebPlayerInterface.log('timeupdate');
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['timeupdate'], myPlayer.currentTime());
                    })


                });
                var muteMode = false;

                function m_setMuteMode(mute) {
                    muteMode = mute;
                    WebPlayerInterface.log('muteMode: ' + muteMode);
                }

                function m_playVideo() {
                    WebPlayerInterface.log('m_playVideo');
                    if (myPlayer != null) {
                        myPlayer.muted(muteMode);
                        myPlayer.play();
                        WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['play'], -1);
                    }
                }

                function m_pauseVideo() {
                    WebPlayerInterface.log('m_pauseVideo');
                    if (!isPauseExecuted) {
                        pauseVideo = true;
                        if (myPlayer != null) {
                            myPlayer.pause();
                        }
                    }
                }

                function m_getShowRemainingTime() {
                    WebPlayerInterface.showRemainingTime();
                }

                callBackNativeMapping['pause'] = 'video_paused'
                callBackNativeMapping['ended'] = 'video_ended'
                callBackNativeMapping['play'] = 'video_started'
                callBackNativeMapping['playing'] = 'video_playing'
                callBackNativeMapping['timeupdate'] = 'video_timeupdate'
                callBackNativeMapping['ads-ad-started'] = 'ad_started'
                callBackNativeMapping['ads-play'] = 'ad_started'
                callBackNativeMapping['ads-pause'] = 'ad_paused'
                callBackNativeMapping['ads-ad-skipped'] = 'ad_skipped'
                callBackNativeMapping['ads-ad-ended'] = 'ad_ended'
                callBackNativeMapping['error'] = 'error'
            </script>
        </div>
    </div>
</body>

</html>