package com.newshunt.common.helper.share;

/**
 * Created by ambar on 14/11/16.
 */

public enum ShareUi {
  FLOATING_ICON("floatingIcon"),
  FLOATING_ICON_BENT_ARROW("floatingIconBentArrow"),
  FLOATING_ICON_W_STRING("floatingIconWString"),
  BOTTOM_BAR("bottomBar"),
  BIG_STORY_CARD("bigStoryCard"),
  DH_BROWSER("dhBrowser"),
  DH_NAVIGATION_DRAWER("dhNavigationDrawer"),
  BUZZ_LIST("buzzListPage"),
  BUZZ_DETAIL("buzzDetailpage"),
  BUZZ_CHANNEL_DETAIL("buzzChannelDetailPage"),
  BUZZ_MY_PLAYLISTS("buzzMyPlaylists"),
  BUZZ_PLAYLIST_DETAIL("buzzPlaylistDetail"),
  BUZZ_SHOW_DETAIL("buzzShowDetail"),
  WEB("web"),
  WEB_W_STRING("webwString"),
  WEB_BENT_ARROW("webBentArrow"),
  LIVE_TV("livetv"),
  COMMENT_BAR_SHARE_ICON("hv_commentbar1"),
  COMMENT_BAR_SHARE_WHATSAPP("wsp_commentbar1"),
  COMMENT_BAR_SHARE_FB("fb_commentbar1"),
  CARD_MENU("card_menu"),
  ONCARD("onCard"),
  VIPANEL("vipanel"),
  DHTV_DETAIL("dhtvDetailpage");

  private String shareUiName;

  ShareUi(String shareUiName) {
    this.shareUiName = shareUiName;
  }

  public static ShareUi fromName(String shareUiName) {
    for (ShareUi shareUi : ShareUi.values()) {
      if (shareUi.shareUiName.equalsIgnoreCase(shareUiName)) {
        return shareUi;
      }
    }
    return null;
  }

  public String getShareUiName() {
    return shareUiName;
  }
}
