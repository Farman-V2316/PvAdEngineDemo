<html>
<head>
    <script type="text/javascript" src="//code.jquery.com/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src=" https://s3-ap-southeast-1.amazonaws.com/playpan.vuclip.com/sdktest/vvan-sdk-v1.0.0.min.js"></script>
</head>
<body>
<div id="player-container">
    <a class="link-log"
      href="https://www.viu.com?utm_source=sensedigital&utm_medium=web&utm_campaign=generic_entertainment_mobile"
       target="_blank"></a>
    <div id="player">Loading the player, please wait...</div>
</div>

<script>
    function getParamValue(paramName) {
        var url = window.location.search.substring(1); //get rid of "?" in querystring
        var qArray = url.split('&'); //get key-value pairs
        for (var i = 0; i < qArray.length; i++) {
            var pArr = qArray[i].split('='); //split key and value
            if (pArr[0] == paramName)
                return pArr[1]; //return value
        }
    };

    var clipid = DH_PLAYER_VIDEO_ID;
    var dimstr = DH_PLAYER_WIDTH+"x"+DH_PLAYER_HEIGHT;
    var player = new VVANPlayer('player',{
        siteid: "dailyhunt",
        slotid: "vvanmain",
        cid: clipid,
        dim: dimstr,
        as: true,
        mt: false
    },function(){
            var thisPlayer = this;
            thisPlayer.onReady(function () {//player's event callback function
                WebPlayerInterface.onReady();
            });
    thisPlayer.onPlay(function (e) {//player's event callback function
        console.log('do something on play event', e);
        WebPlayerInterface.onPlayerStateChange(e, '-1');
    });

    thisPlayer.onPlayerStateChange(function(e) {
    var state = e;
        if(e == 'play') {
          state='video_started';
        } else if(e == 'firstFrame') {
        } else if(e == 'time') {
          state='video_playing';
        } else if(e == 'pause') {
          state='video_paused';
        } else if(e == 'beforePlay') {
        } else if(e == 'providerFirstFrame') {
        } else if(e == 'seek') {
          state='seek';
        } else if(e == 'seeked') {
          state='seeked';
        } else if(e == 'beforeComplete') {
        } else if(e == 'complete') {
          state = 'video_ended';
        } else if(e == 'playlistComplete') {
        } else if(e == 'remove') {
          state='removed';
        } else if(e == 'fullscreen'){
          state='on_full_screen_click';
        }
        //Ads
        else if(e == 'adPlay') {
          state = 'ad_started';
        } else if(e == 'adTime') {
          state = 'ad_playing';
        } else if(e == 'adPause') {
          state = 'ad_paused';
        } else if(e == 'adComplete') {
          state = 'ad_ended';
        } else if(e == 'adSkipped') {
          state = 'ad_skipped';
        }
        WebPlayerInterface.onPlayerStateChange(state, '-1');
    });

    });

    function m_loadVideo(cid) {//load another video by video id.
        if (!cid) return;
        player.loadPlayer({
            siteid: "vuclip",
            slotid: "test",
            cid: cid,
            as: true,
            mt: false,
        }, function () {
            player.onReady(function () {
                WebPlayerInterface.onReady();
            });
        })
    }

    function m_stopVideo() {
        console.log("m_stopVideo------------>");
        player.stop();
    }
    function m_playVideo() {
        console.log("m_playVideo------------>");
        player.play();
    }
    function m_seekVideo(position){
        console.log("m_seekVideo------------>");
        player.seek(position);
    }
    function m_pauseVideo() {
        console.log("m_pauseVideo------------>");
        player.pause(true);
    }
    function m_fullScreen(isFullscreen){
        console.log("m_fullScreen------------>");
        player.fullScreen(isFullscreen);
    }
    function m_setMuteMode(state){
        console.log("m_setMuteMode------------>");
        player.mute(state);
    }

    function m_setControlState(state){
        console.log("m_setControlState------------>");
        player.playerInstance.setControls(state);
    }

    player.onPause(function (e) {
        console.log('do something on pause event', e);
    });
    player.onPlay(function (e) {
        console.log('do something on play event', e);
    });

</script>
</body>
</html>