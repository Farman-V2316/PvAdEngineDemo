<!DOCTYPE html>
<html>

<head>
    <title> Ventuno Embed Code </title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
</head>

<body topmargin="0" leftmargin="0">
<div id="vt-video-player"></div>
<script type="text/javascript">
    var callBackNativeMapping = new Object();
    callBackNativeMapping['resume'] = 'video_started'
    callBackNativeMapping['pause'] = 'video_paused'
    callBackNativeMapping['content_start'] = 'video_started'
    callBackNativeMapping['content_firstquartile'] = 'video_playing'
    callBackNativeMapping['content_midpoint'] = 'video_playing'
    callBackNativeMapping['content_thirdquartile'] = 'video_playing'
    callBackNativeMapping['completed'] = 'video_ended'
    callBackNativeMapping['buffering'] = 'buffering'
    callBackNativeMapping['buffering_complete'] = 'finished_buffering'
    callBackNativeMapping['seek'] = 'seek'
    callBackNativeMapping['seeked'] = 'seeked'
    callBackNativeMapping['ad_start'] = 'ad_started'
    callBackNativeMapping['ad_firstquartile'] = 'ad_playing'
    callBackNativeMapping['ad_midpoint'] = 'ad_playing'
    callBackNativeMapping['ad_thirdquartile'] = 'ad_playing'
    callBackNativeMapping['ad_complete'] = 'ad_ended'
    callBackNativeMapping['fullscreen'] = 'on_full_screen_click'
    callBackNativeMapping['error'] = 'error'
    callBackNativeMapping['mute'] = 'Mute'
    callBackNativeMapping['unmute'] = 'Unmute'

    /* player events */
    var MS_ERROR = 'error',
        MS_PAUSE = 'pause',
        MS_RESUME = 'resume',
        MS_COMPLETED = 'completed',
        MS_REPLAY = 'replay',
        MS_MUTE = 'mute',
        MS_UNMUTE = 'unmute',
        MS_FULLSCREEN = 'fullscreen',
        MS_CONTENT_START = 'content_start',
        MS_CONTENT_FIRSTQUARTILE = 'content_firstquartile',
        MS_CONTENT_MIDPOINT = 'content_midpoint',
        MS_CONTENT_THIRDQUARTILE = 'content_thirdquartile',
        MS_CONTENT_COMPLETE = 'content_complete',
        MS_AD_START = 'ad_start',
        MS_AD_FIRSTQUARTILE = 'ad_firstquartile',
        MS_AD_MIDPOINT = 'ad_midpoint',
        MS_AD_THIRDQUARTILE = 'ad_thirdquartile',
        MS_AD_COMPLETE = 'ad_complete',
        MS_SEEK = 'seek',
        MS_SEEKED = 'seeked',
        MS_BUFFERING = 'buffering',
        MS_BUFFERING_COMPLETE = 'buffering_complete';

    /* PLAYER INTEGRATION CODE (PIC)
    passing additional 'get_player_api' parameter in the PIC */
    window.__ventunoplayer = window.__ventunoplayer || [];
    window.__ventunoplayer.push({
        holder_id: 'vt-video-player',
        video_key: 'DH_PLAYER_VIDEO_ID',
        width: 'DH_PLAYER_WIDTH',
        height: 'DH_PLAYER_HEIGHT',
        player_type: 'vp',
        ratio: '16:9',
        autoplay: 'true',
        get_player_api: getPlayerAPI
    });
    window.__ventunoplayer.push({
        flush: true
    });


    /* API SETUP */
    var player;

    function getPlayerAPI(_player) {
        player = _player;
        player.addEventListener(MS_ERROR, listner);
        player.addEventListener(MS_RESUME, listner);
        player.addEventListener(MS_PAUSE, listner);
        player.addEventListener(MS_RESUME, listner);
        player.addEventListener(MS_COMPLETED, listner);
        player.addEventListener(MS_REPLAY, listner);
        player.addEventListener(MS_MUTE, listner);
        player.addEventListener(MS_UNMUTE, listner);
        player.addEventListener(MS_FULLSCREEN, listner_dummy);
        player.addEventListener(MS_CONTENT_START, listner);
        player.addEventListener(MS_CONTENT_FIRSTQUARTILE, listner);
        player.addEventListener(MS_CONTENT_MIDPOINT, listner);
        player.addEventListener(MS_CONTENT_THIRDQUARTILE, listner);
        player.addEventListener(MS_CONTENT_COMPLETE, listner);
        player.addEventListener(MS_AD_START, listner);
        player.addEventListener(MS_AD_FIRSTQUARTILE, listner);
        player.addEventListener(MS_AD_MIDPOINT, listner);
        player.addEventListener(MS_AD_THIRDQUARTILE, listner);
        player.addEventListener(MS_AD_COMPLETE, listner);
        player.addEventListener(MS_SEEK, listner);
        player.addEventListener(MS_SEEKED, listner);
        player.addEventListener(MS_BUFFERING, listner);
        player.addEventListener(MS_BUFFERING_COMPLETE, listner);

    };

    var isReady = false;


    function listner(_evnts) {
        WebPlayerInterface.onPlayerStateChange(callBackNativeMapping[_evnts.type], '-1');
        WebPlayerInterface.log(_evnts.type);
        if (callBackNativeMapping[_evnts.type] == "video_started" || callBackNativeMapping[_evnts.type] == "ad_started") {
            if (!isReady) {
                WebPlayerInterface.onReady();
                isReady = true;
            }
        }
    };

  function m_pauseVideo() {
        WebPlayerInterface.log('m_pauseVideo');
        if (player != null) {
            player.pause();
        }
   };

   function m_playVideo() {
         WebPlayerInterface.log('m_playVideo');
         if (player != null) {
            player.resume();
         }
   };

    function listner_dummy(_evnts) {

    };
</script>
<script type="text/javascript" src="https://pl.ventunotech.com/plugins/cntplayer/ventunoSmartPlayer.js"></script>
</body>

</html>