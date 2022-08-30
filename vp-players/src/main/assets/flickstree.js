<!DOCTYPE html>

<html>

<head>
	<title>Flickstree</title>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<script>
        var isAutoplayVideo = false;
        try {
            isAutoplayVideo = WebPlayerInterface.isAutoplayVideo();
        } catch (err) {}
        var playerUrl = "https://content.jwplatform.com/players/DH_PLAYER_VIDEO_ID.js";
        if (isAutoplayVideo) {
            var hyphenPosition = playerUrl.lastIndexOf("-");
            playerUrl = playerUrl.substring(0, hyphenPosition);
            playerUrl = playerUrl + "-OvqMo5Wn.js"; // add static without ad player id
        }
        document.write(unescape('%3Cscript src="' + playerUrl + '"%3E%3C/script%3E'));
    </script>
</head>

<body>

<div id="player"></div>
<script type="text/javascript">
        var callBackNativeMapping = new Object();
        var player = jwplayer();
        var playVideo = false;

        player.on('pause', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['pause'], -1);
            if (playVideo && player != null) {
                player.play(true);
                playVideo = false;
            }
        });

        player.on('complete', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['complete'], -1);
        });

        player.on('buffer', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['buffer'], -1);
        });

        player.on('finish_buffering', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['finish_buffering'], -1);
        });

        player.on('seek', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['seek'], -1);
        });

        player.on('seeked', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['seeked'], -1);
        });

        player.on('play', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['play'], -1);
        });
        player.on('playing', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['playing'], -1);
        });
        player.on('time', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['time'], player.getPosition());
        });
        player.on('ads-ad-started', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['ads-ad-started'], -1);
        });
        player.on('adPlay', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['adPlay'], -1);
        });
        player.on('adPause', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['adPause'], -1);
        });
        player.on('adSkipped', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['adSkipped'], -1);
        });
        player.on('adComplete', function(event) {
            console.log(event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['adComplete'], -1);
        });
        player.on('displayClick', function(event) {
            console.log("displayClick " + event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['displayClick'], -1);
        });

        player.on('error', function(event) {
            console.log("error " + event);
            WebPlayerInterface.onPlayerStateChangeWithPlayerCurTime(callBackNativeMapping['error'], -1);
        });

        player.on('ready', function(event) {
            console.log(event);
            WebPlayerInterface.log('PLAYER ON READY');
            WebPlayerInterface.onReady();
            WebPlayerInterface.shouldShowThumbnailOnError();
        });

        player.on('mute', function(event) {
            console.log(event);
            if (null != player) {
                WebPlayerInterface.onMuteStateChanged(player.getMute());
            }
        });

        var muteMode = false;

        function m_playVideo() {
            WebPlayerInterface.log('PLAY VIDEO');
            if (null != player) {
                player.setMute(muteMode);
                player.play(true);
                playVideo = true;
            }
        }


        function m_setMuteMode(mute) {
            if (null != player) {
                muteMode = mute;
                player.setMute(mute);
            }
        }

        function m_pauseVideo() {
            WebPlayerInterface.log('PAUSE VIDEO');
            if (null != player) {
                player.pause(true);
                playVideo = false;
            }
        }

        function m_setControlState(state) {
            WebPlayerInterface.log('CONTROL STATE VIDEO');
            console.log("m_setControlState------------>");
            player.setControls(state);
        }

        function m_getShowRemainingTime() {
            WebPlayerInterface.showRemainingTime();
        }

        callBackNativeMapping['pause'] = 'video_paused'
        callBackNativeMapping['complete'] = 'video_ended'
        callBackNativeMapping['buffer'] = 'buffering'
        callBackNativeMapping['finish_buffering'] = 'finish_buffering'
        callBackNativeMapping['seek'] = 'seek'
        callBackNativeMapping['seeked'] = 'seeked'
        callBackNativeMapping['play'] = 'video_started'
        callBackNativeMapping['playing'] = 'video_playing'
        callBackNativeMapping['time'] = 'video_timeupdate'
        callBackNativeMapping['ads-ad-started'] = 'ad_started'
        callBackNativeMapping['adPlay'] = 'ad_started'
        callBackNativeMapping['adPause'] = 'ad_paused'
        callBackNativeMapping['adSkipped'] = 'ad_skipped'
        callBackNativeMapping['adComplete'] = 'ad_ended'
        callBackNativeMapping['error'] = 'error'
        callBackNativeMapping['displayClick'] = 'displayClick'
    </script>
</body>

</html>