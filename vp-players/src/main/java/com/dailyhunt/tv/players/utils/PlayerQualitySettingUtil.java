package com.dailyhunt.tv.players.utils;

import com.dailyhunt.tv.exolibrary.VideoQualitySettings;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerVideoQuality;

/**
 * Created by Jayanth on 09/05/18.
 */
public class PlayerQualitySettingUtil {

  //(TODO) Santosh.Kulkarni once we create tv-commons module then we dont require
  public static VideoQualitySettings setTVVideoQualitySettings(PlayerVideoQuality quality) {
    VideoQualitySettings videoQualitySettings = new VideoQualitySettings();

    videoQualitySettings.setHlsMaxTimeForSwitchDownMs(quality.getHlsMaxTimeForSwitchDownMs());
    videoQualitySettings.setHlsMinTimeForSwitchUpMs(quality.getHlsMinTimeForSwitchUpMs());
    videoQualitySettings.setNomialBitRateForHLSFirstvariant(
        quality.getNomialBitRateForHLSFirstvariant());

    videoQualitySettings.setMinBufferSize(quality.getBufferMinSize());
    videoQualitySettings.setMaxBufferSize(quality.getBufferMaxSize());
    return videoQualitySettings;
  }
}
