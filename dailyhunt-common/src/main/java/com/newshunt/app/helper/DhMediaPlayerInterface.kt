package com.newshunt.app.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import com.dailyhunt.tv.exolibrary.entities.StreamConfigAsset
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.app.analytics.AudioPlayerAnalyticsEventParams
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.helper.VideoPlayBackTimer
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.HashMap

/**
 * @author anshul.jain
 */
interface DhMediaPlayerInterface {

    fun start(audioStreamUrl: String, audioLanguage: String? = null, itemId: String? = null,
              trigger: AudioStateTrigger? = null)

    fun stop(trigger: AudioStateTrigger? = null)
}

enum class PlayerAudioEndAction(val value: String) {
    STOP("STOP"), RESET("RESET"), PAUSE("PAUSE")
}

enum class PlayerAudioStartAction(val value: String) {
    RESET("RESET"), PLAY("PLAY"), RESUME("RESUME")
}

enum class AudioAnalyticsEvent : NhAnalyticsEvent {

    AUDIO_PLAYED;

    override fun isPageViewEvent() = false

    override fun toString() = this.name
}

enum class AudioStateTrigger : Serializable{
    USER, //Play or stop triggered by user action
    PLAYER, //Play or stop triggered by media player while we reset to catch up with live time
    BACKEND, //Play or stop by Backend
    FOCUS_CHANGE, //Play or stop when focus gain or lost
    ERROR, //play or stopped on network error
    DEPENDENT_REMOVED //Play or stop because of dependency change for eg, video associated with
    // audio may been have stopped, because of that audio has to be stopped
}

interface DHAudioPlayerCallback {

    fun onAudioBufferingStarted() {}

    fun onAudioBufferingEnded(bufferingEndedToPlay: Boolean) {}

    fun onAudioStarted(trigger: AudioStateTrigger?) {}

    fun onAudioStopped(trigger: AudioStateTrigger?) {}

    fun onAudioComplete(trigger: AudioStateTrigger?) {}

}

object DHMediaPlayerManger : DhMediaPlayerInterface, DhAudioFocusManagerInterface, BufferedStateInterface,Player.EventListener {

    private val TAG = "DHMediaPlayerManger"
    private var audioManager = DhAudioManager(this)
    private var dhAudioPlayerCallbackRef: WeakReference<DHAudioPlayerCallback>? = null
    private var mediaPlayer: SimpleExoPlayer? = null
    private var audioLanguage: String? = null
    private var itemId: String? = null
    private var currentStreamUrl: String? = null
    val videoPlayBackTimer = VideoPlayBackTimer()
    private var audioEndAction = PlayerAudioEndAction.STOP
    private var audioStartAction = PlayerAudioStartAction.PLAY
    private val bufferedStateManager = BufferedStateManager(this)
    private var audioStateTrigger : AudioStateTrigger? = null
    private var isPrepared = false

    fun setDhAudioManagerCallback(dhAudioPlayerCallback: DHAudioPlayerCallback?) {
        dhAudioPlayerCallback ?: return
        dhAudioPlayerCallbackRef = WeakReference(dhAudioPlayerCallback)
    }

    override fun start(audioStreamUrl: String, audioLanguage: String?, itemId: String?,
                       trigger: AudioStateTrigger?) {
        start(audioStreamUrl, audioLanguage, PlayerAudioStartAction.PLAY, itemId, trigger)
    }

    private fun start(audioStreamUrl: String, audioLanguage: String?, action:
    PlayerAudioStartAction, itemId : String?, trigger: AudioStateTrigger?) {
        this.currentStreamUrl = audioStreamUrl
        this.audioLanguage = audioLanguage
        this.itemId = itemId
        audioStateTrigger = trigger
        releaseMediaPlayer()

        setStartAction(action)
        bufferedStateManager.audioStartTime = 0L
        bufferedStateManager.start()
        mediaPlayer = ExoPlayerUtils.buildAudioPlayer(StreamConfigAsset.DEFAULT)
        val mediaSource = ExtractorMediaSource(Uri.parse(audioStreamUrl),
            DefaultDataSourceFactory(CommonUtils.getApplication(),  Util.getUserAgent(CommonUtils.getApplication(),
                getApplicationName(CommonUtils.getApplication()))),
            DefaultExtractorsFactory(),
            null, null)
        mediaPlayer?.prepare(mediaSource)
        mediaPlayer?.addListener(this)
        dhAudioPlayerCallbackRef?.get()?.onAudioBufferingStarted()
        fireAudioStartIntent()
    }

    fun fireAudioStartIntent() {
        val audioStartIntent = Intent(DHConstants.INTENT_STICKY_AUDIO_STARTED)
        if (AppConfig.getInstance() != null) {
            audioStartIntent.`package` = AppConfig.getInstance().packageName
        }
        CommonUtils.getApplication().sendBroadcast(audioStartIntent)
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Logger.e(TAG , "Play error received")
        onBufferingEnded(false)
        dhAudioPlayerCallbackRef?.get()?.onAudioStopped(AudioStateTrigger.ERROR)
    }

    private fun getApplicationName(context: Context): String {
      val applicationInfo = context.applicationInfo
      val stringId = applicationInfo.labelRes
      return if (stringId == 0)
        applicationInfo.nonLocalizedLabel.toString()
      else
        context.getString(stringId)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Logger.e(TAG, "player state changed called with $playWhenReady and state as $playbackState")
        if (playbackState == Player.STATE_READY) {
            Logger.i(TAG, "Player state ready")
            if (!isPrepared && mediaPlayer?.playWhenReady == false) {
                Logger.i(TAG, "Playing the audio")
                mediaPlayer?.playWhenReady = true
                audioManager.getAudioFocusRequest()
                bufferedStateManager.audioStartTime = System.currentTimeMillis()
                isPrepared = true
            }
            onBufferingEnded(true)
            dhAudioPlayerCallbackRef?.get()?.onAudioStarted(audioStateTrigger)
        } else if (playbackState == Player.STATE_BUFFERING) {
            Logger.i(TAG, "Player state buffering")
            onBufferingStarted()
        } else {
            Logger.i(TAG, "Player state : " + if (playbackState == Player.STATE_ENDED)
                "audioStreamEnded" else "Player is idle")
            onBufferingEnded(false)
            if (playbackState == Player.STATE_ENDED) {
                dhAudioPlayerCallbackRef?.get()?.onAudioComplete(audioStateTrigger)
            } else {
                dhAudioPlayerCallbackRef?.get()?.onAudioStopped(audioStateTrigger)
            }
        }
    }

    override fun stop(trigger: AudioStateTrigger?) {
       stop(PlayerAudioEndAction.STOP, trigger)
    }

    private fun stop(action: PlayerAudioEndAction, trigger: AudioStateTrigger?) {
        audioStateTrigger = trigger
        audioManager.releaseAudioFocus()
        releaseMediaPlayer()
        setEndAction(action)
        bufferedStateManager.stop()
        dhAudioPlayerCallbackRef?.get()?.onAudioStopped(trigger)
    }

    private fun releaseMediaPlayer() {
        setEndAction(PlayerAudioEndAction.STOP)
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
    }

    override fun onAudioFocusRequestGranted() {
        Logger.d(TAG, "audioFocus  request granted. Playing the audio")
        mediaPlayer?.playWhenReady = true
    }

    override fun onAudioFocusGained() {
        Logger.d(TAG, "audioFocus  Gained. Playing the audio")
        mediaPlayer?.playWhenReady = true
        setStartAction(PlayerAudioStartAction.RESUME)
    }

    override fun onAudioFocusLost() {
        Logger.d(TAG, "audioFocus Lost. Hence stopped playing the audio")
        stop(AudioStateTrigger.FOCUS_CHANGE)
    }

    override fun onAudioFocusLostTransient() {
        Logger.d(TAG, "audioFocus Transient Lost hence pausing")
        mediaPlayer?.playWhenReady = false
        setEndAction(PlayerAudioEndAction.PAUSE)
    }

    fun setStartAction(startAction: PlayerAudioStartAction) {
        audioStartAction = startAction
        videoPlayBackTimer.start()
    }

    fun setEndAction(endAction: PlayerAudioEndAction) {
        audioEndAction = endAction
        videoPlayBackTimer.stop()
        val playbackDuration = videoPlayBackTimer.getTotalTime()
        videoPlayBackTimer.reset()
        if (playbackDuration <= 0L) {
            //resetting values to default
            audioEndAction = PlayerAudioEndAction.STOP
            return
        }

        val map = HashMap<NhAnalyticsEventParam, Any>()
        // Since it is a live audio stream the start time and the end time is 0
        map[AudioPlayerAnalyticsEventParams.START_TIME] = 0
        map[AudioPlayerAnalyticsEventParams.END_TIME] = 0

        map[AudioPlayerAnalyticsEventParams.PLAYER_TYPE] = "ANDROID MEDIA PLAYER"
        map[AudioPlayerAnalyticsEventParams.END_ACTION] = audioEndAction.value
        map[AudioPlayerAnalyticsEventParams.PLAYBACK_DURATION] = playbackDuration
        map[AudioPlayerAnalyticsEventParams.ITEM_CATEGORY_ID] = "Audio Commentary"
        map[AnalyticsParam.ITEM_LANGUAGE] = audioLanguage
                ?: UserPreferenceUtil.getUserNavigationLanguage()
        map[AnalyticsParam.ITEM_ID] = itemId ?: Constants.EMPTY_STRING
        map[AudioPlayerAnalyticsEventParams.START_ACTION] = audioStartAction.value

        val operatorName = DeviceInfoHelper.getOperatorName(CommonUtils.getApplication())
        if (!DataUtil.isEmpty(operatorName)) {
            map[AnalyticsParam.NETWORK_SERVICE_PROVIDER] = operatorName
        }

        val event = AudioAnalyticsEvent.AUDIO_PLAYED

        //SearchAnalyticsHelper.addSearchParams(section, map)
        //log video event
        AnalyticsClient.log(event, NhAnalyticsEventSection.APP, map)

        //resetting values to default
        audioEndAction = PlayerAudioEndAction.STOP
    }

    override fun getCurrentPosition(): Int {
        return (mediaPlayer?.currentPosition ?: 0).toInt()
    }

    override fun onBufferingStarted() {
        Logger.d(TAG, "Buffering started..")
        dhAudioPlayerCallbackRef?.get()?.onAudioBufferingStarted()
    }

    override fun onBufferingEnded(bufferingEndedToPlay : Boolean) {
        Logger.d(TAG, "Buffering ended..")
        dhAudioPlayerCallbackRef?.get()?.onAudioBufferingEnded(bufferingEndedToPlay)
    }

    override fun refreshAudio() {
        Logger.d(TAG, "Refresh Audio Called..")
        stop(PlayerAudioEndAction.RESET, AudioStateTrigger.PLAYER)
        currentStreamUrl?.let {
            start(it, audioLanguage, PlayerAudioStartAction.RESET, itemId, AudioStateTrigger.PLAYER)
        }
    }
}

class BufferedStateManager(private val bufferedStateInterface: BufferedStateInterface, var
audioStartTime: Long = 0) {

    private var lastPlayedPosition = -1
    private var isBuffering = false
    private val handler = Handler()
    private val DURATION_FOR_CHECKING_BUFFERING_STATE = 1000L
    private val DURATION_FOR_REFRESH_AUDIO = PreferenceManager.getPreference(GenericAppStatePreference.DURATION_FOR_AUDIO_COMMENTARY_REFRESH, 300) * 1000
    private val TAG = "BufferedStateManager"

    fun start() {
        lastPlayedPosition = -1
        isBuffering = false
        checkBufferedState()
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun checkBufferedState() {
        val currentPosition = bufferedStateInterface.getCurrentPosition()

        if (audioStartTime != 0L) {
            val expectedAudioPlayTime = System.currentTimeMillis() - audioStartTime
            Logger.d(TAG, " The expected audio time is $expectedAudioPlayTime and " +
                    "current time is $currentPosition")
            if (expectedAudioPlayTime - currentPosition > DURATION_FOR_REFRESH_AUDIO) {
                bufferedStateInterface.refreshAudio()
                return
            }
        }
        lastPlayedPosition = currentPosition
        handler.postDelayed({ checkBufferedState() }, DURATION_FOR_CHECKING_BUFFERING_STATE)
    }
}

interface BufferedStateInterface {

    fun getCurrentPosition(): Int

    fun onBufferingStarted()

    fun onBufferingEnded(bufferingEndedToPlay: Boolean)

    fun refreshAudio()
}