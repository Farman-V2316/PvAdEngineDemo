<!DOCTYPE html>
<html>
   <head></head>
   <body style="margin: 0; padding: 0">
      <div style="width:DH_PLAYER_WIDTHpx; height:DH_PLAYER_HEIGHTpx;" id="rumblePlayer">
      </div>
      <script type="text/javascript">
        ! function(r, u, m, b, l, e) {
                r._Rumble = b, r[b] || (r[b] = function() {
                    (r[b]._ = r[b]._ || []).push(arguments);
                    if (r[b]._.length == 1) {
                        l = u.createElement(m), e = u.getElementsByTagName(m)[0],
                            l.async = 1, l.src = "https://rumble.com/embedJS/ugnz8" +
                            (arguments[1].video ? '.' + arguments[1].video : '') + "/?url=" +
                            encodeURIComponent(location.href) + "&args=" +
                            encodeURIComponent(JSON.stringify([].slice.apply(arguments))),
                            e.parentNode.insertBefore(l, e)
                    }
                })
            }(window, document, "script", "Rumble");
            var player;
            Rumble("play", {
        		"video": "DH_PLAYER_VIDEO_ID",
        		"div": "rumblePlayer",
        		"api": function(api) {
        		  player = api;
        		  WebPlayerInterface.onReady();
              api.on('loadVideo', function() {
                WebPlayerInterface.log('loadVideo');
              });
        			api.on('preplay', function() {
                WebPlayerInterface.log('preplay');
              });
        			api.on('play', function() {
        				WebPlayerInterface.log('play');
        			});
        			api.on('pause', function() {
                WebPlayerInterface.log('pause');
              });
        			api.on('videoEnd', function() {
                WebPlayerInterface.log('videoEnd');
              });

              api.on('preAd', function() {
                WebPlayerInterface.log('preAd');
              });
              api.on('adError', function() {
                WebPlayerInterface.log('adError');
              });
              api.on('adImpression', function() {
                WebPlayerInterface.log('adImpression');
              });
              api.on('adClick', function() {
                WebPlayerInterface.log('adClick');
              });

              api.on('mute', function() {
                WebPlayerInterface.log('mute');
              });
              api.on('volumeChange', function() {
                WebPlayerInterface.onMuteStateChanged(player.getMuted());
              });
              api.on('resize', function() {
                WebPlayerInterface.log('resize');
              });
              api.on('ui', function() {
                WebPlayerInterface.log('ui');
              });
        		}
        	});

        	function m_playVideo() {
              if (null != player) {
                  player.play(true);
              }
          }

          function m_pauseVideo() {
              if (null != player) {
                  player.pause();
              }
          }

          function m_setMuteMode(mute) {
              if (null != player) {
                  if(mute) {
                    player.mute();
                  } else {
                    player.unmute();
                  }
              }
          }

          function m_getShowRemainingTime() {

          }

      </script>
   </body>
</html>