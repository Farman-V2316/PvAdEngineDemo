<html><head>
<style>
    body{margin:0}
    iframe{margin:0;}
</style>
</head>
<script type="text/javascript">
    var tag = document.createElement('script');
    tag.id = 'iframe-demo';
    tag.src = 'https://www.youtube.com/iframe_api';
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

    var player;
    function onYouTubeIframeAPIReady() {
      player = new YT.Player('dh-tv-iframe', {
          events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange,
            'onError': onPlayerError
          }
      });
    }

    function onPlayerReady(event) {
      WebPlayerInterface.onReady();
    }

    function onPlayerError(event) {
        WebPlayerInterface.onError(event);
     }

    function onPlayerStateChange(event) {
        var str;
        WebPlayerInterface.log('event : ' + event.data);
        if (event.data == YT.PlayerState.PLAYING){
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime('video_started',player.getCurrentTime());
        }else if(event.data == YT.PlayerState.ENDED){
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime('video_ended',player.getCurrentTime());
        }else if(event.data == YT.PlayerState.PAUSED){
           WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime('video_paused',player.getCurrentTime());
        }else if(event.data == YT.PlayerState.BUFFERING){
           WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime('buffering',player.getCurrentTime());
        }
    }

    var muteMode = false;
    function m_setMuteMode(mute) {
        if (null != player) {
            muteMode = mute;
            if(mute) {
                player.mute();
            } else {
                player.unMute()
            }
        }
    }


    function m_playVideo(){
      if(player != null){
          m_setMuteMode(muteMode);
          player.playVideo();
          var myvideo = document.getElementsByTagName('video')[0];
          if(myVideo != null){
            myvideo.play();
          }
       }
    }

    function m_pauseVideo(){
      if(player != null){
          player.pauseVideo();
      }
    }

    function m_playerMuteState() {
      if(player != null){
          WebPlayerInterface.onMuteStateChanged(player.isMuted());
      }
    }

</script>
<iframe id="dh-tv-iframe" width="100%" height="100%" src="https://www.youtube.com/embed/DH_PLAYER_VIDEO_ID?autoplay=1&enablejsapi=1&fs=1" frameborder="0"    style="margin:0;padding:0;" allowfullscreen></iframe></html>