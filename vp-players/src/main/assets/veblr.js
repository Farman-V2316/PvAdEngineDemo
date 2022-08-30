<!DOCTYPE html>
<html>
<head>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    <script type="text/javascript" src="https://vbcdn.com/js/iframe_player_events.js?v=1.1.7"></script>
    <script type="text/javascript">

        if (typeof veblrPlayerEvents == 'function') {
            veblrPlayerEvents = new veblrPlayerEvents({
                videoPlayerOnReady:'videoPlayerOnReadyFn',
                videoCanPlay:'videoCanPlayFn',
                videoStart:'videoStartFn',
                videoFirstQuartile:'videoFirstQuartileFn',
                videoMidQuartile:'videoMidQuartileFn',
                videoThirdQuartile:'videoThirdQuartileFn',
                videoEnded:'videoEndedFn',
                videoPause:'videoPauseFn',
                videoSeeking:'videoSeekingFn',
                videoSeeked:'videoSeekedFn',
                videoVolumeChange:'videoVolumeChangeFn'
            });

            function videoPlayerOnReadyFn(){
                //video player ready
                WebPlayerInterface.onReady();
            }

            function videoCanPlayFn(){
                //video is ready to start (when it has buffered enough to begin)
            }

            function videoStartFn() {
                //video start playing
                WebPlayerInterface.onPlayerStateChange('video_started', '-1');
            }

            function videoFirstQuartileFn() {
                //video first quartile completed
                WebPlayerInterface.onPlayerStateChange('first_quartile', '-1');
            }

            function videoMidQuartileFn() {
                //video mid quartile completed
                WebPlayerInterface.onPlayerStateChange('second_quartile', '-1');
            }

            function videoThirdQuartileFn() {
                //video third quartile completed
                WebPlayerInterface.onPlayerStateChange('third_quartile', '-1');
            }

            function videoEndedFn(){
                //video ended
                WebPlayerInterface.onPlayerStateChange('video_ended', '-1');
            }

            function videoPauseFn(){
                //video paused
                WebPlayerInterface.onPlayerStateChange('video_paused', '-1');
            }

            function videoSeekingFn(){
                //video seeking
                WebPlayerInterface.onPlayerStateChange('videoseek', '-1');
            }

            function videoSeekedFn(){
                //video seeked
                WebPlayerInterface.onPlayerStateChange('videoseeked', '-1');
            }

            function videoVolumeChangeFn(){
                //video volume changed
                WebPlayerInterface.onPlayerStateChange('videoVolumeChangeFn', '-1');
            }
        }

        function m_playVideo() {
            div_id_req = "vPlayer";
            func_req = "playVideo"
            remoteServerPostMsg(div_id_req, func_req);
        }

        function m_pauseVideo() {
            div_id_req = "vPlayer";
            func_req = "pauseVideo"
            remoteServerPostMsg(div_id_req, func_req);
        }

        function m_setMuteMode(mute) {
            div_id_req = "vPlayer";
            if(mute == true) {
                func_req = "muteVideo"
            } else {
                func_req = "unmuteVideo"
            }
            remoteServerPostMsg(div_id_req, func_req);
        }
    </script>
</head>
<body style="margin: 0; padding: 0">
  <div style="width:DH_PLAYER_WIDTHpx; height:DH_PLAYER_HEIGHTpx;" id="vPlayer">
    <iframe src="https://veblr.com/embed/DH_PLAYER_VIDEO_ID?autoplay=true&autoplaynext=false&enablejsapi=1" type="text/html" scrolling="no" frameborder="0" allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true" allowfullscreen style="width:100%; height:100%;" id="player1" name="player1"></iframe>
</div>

</body>
</html>