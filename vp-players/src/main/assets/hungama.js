<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hungama player Embed</title>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=yes">
    <script type="text/javascript"
            src="http://player.hungama.com/lap/player_in_iframe/hungama_player_embed.js"></script>
</head>
<body>
<div id="playerDiv" style="width:DH_PLAYER_WIDTH;height:DH_PLAYER_HEIGHT"></div>
<script>
        embedPlayer({
			"elementId":"playerDiv",
			"url":"DH_PLAYER_VIDEO_ID"
		});

    var callBackNativeMapping = new Object();
        var eventMethod = window.addEventListener ? "addEventListener" : "attachEvent";
        var eventer = window[eventMethod];
        var messageEvent = eventMethod == "attachEvent" ? "onmessage" : "message";
        var isReady = false;
        eventer(messageEvent, function(e) {
            var eventType = e.data;
            WebPlayerInterface.onPlayerStateChange(callBackNativeMapping[eventType], '-1');
            switch (eventType) {
                case "videostarted":
                case "play":
                    console.log("iframe video:  playing");
                    if(!isReady) {
                        WebPlayerInterface.onReady();
                        isReady = true;
                    }
                    break;
                case "pause":
                    console.log("iframe video:  Paused ");
                    break;
                case "buffering":
                    console.log("iframe video:  buffering");
                    break;
                case "ended":
                    console.log("iframe video:  ended");
                    break;
                case "aderror":
                    console.log("iframe video:  add error");
                    break;
                case "adplay":
                    console.log("iframe video:  ad play");
                    break;
                case "adstart":
                    console.log("iframe video:  adstart");
                    break;
                case "adpause":
                    console.log("iframe video:  adpause");
                    break;
                case "adskip":
                    console.log("iframe video:  adskip");
                    break;
                case "adcomplete":
                    console.log("iframe video:  adcomplete");
                    break;
                case "videoseek":
                    console.log("iframe video:  videoseek");
                    break;
                case "videoseeked":
                    console.log("iframe video:  videoseeked");
                    break;
                case "enterfs":
                    console.log("iframe video:  enterfs");
                    break;
                case "exitfs":
                    console.log("iframe video:  exitfs");
                    break;
            }
        }, false);


    function m_pauseVideo() {
        console.log("m_pauseVideo------------>");
        hungamaPlayer('pause');
    }

    function m_setMuteMode(mute) {
      WebPlayerInterface.log('m_setMuteMode: ' + mute);
    }

    function m_playVideo() {
      console.log("m_pauseVideo------------>");
      hungamaPlayer('play');
    }

    function m_setControlState(state) {

    }

    function m_getShowRemainingTime() {

    }

    callBackNativeMapping['pause']='video_paused'
    callBackNativeMapping['ended']='video_ended'
    callBackNativeMapping['videostarted']='video_started'
    callBackNativeMapping['play']='video_started'
    callBackNativeMapping['videoseek']='seek'
    callBackNativeMapping['videoseeked']='seeked'
    callBackNativeMapping['onAdStarted']='ad_started'
    callBackNativeMapping['adstart']='ad_playing'
    callBackNativeMapping['adpause']='ad_paused'
    callBackNativeMapping['adcomplete']='ad_ended'
    callBackNativeMapping['adskip']='ad_skipped'


</script>
</body>
</html>
