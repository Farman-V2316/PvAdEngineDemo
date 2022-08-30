package com.newshunt.common.helper.share;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.util.R;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.helper.share.ShareApplication;
import com.newshunt.dataentity.common.model.entity.ShareTextMappingResponse;

/**
 * Help choose right app to share.
 *
 * @author ambar
 */
public class ShareFactory {

  private static final String TAG = "ShareFactory";

  public static ShareHelper getShareHelper(String packageName, Activity activity,
                                           Intent sendIntent) {
    return getShareHelper(packageName, activity, sendIntent, null);
  }

  public static ShareHelper getShareHelper(String packageName, Activity activity,
                                           Intent sendIntent, ShareContent shareContent) {
    return getShareHelper(packageName, activity, sendIntent, shareContent, true);
  }

  public static ShareHelper getShareHelper(String packageName, Activity activity, Intent sendIntent,
                                           ShareContent shareContent, boolean appendShareSource) {
    return getShareHelper(packageName, activity, sendIntent, shareContent, appendShareSource,
        false);
  }

  public static ShareHelper getShareHelper(String packageName, Activity activity, Intent sendIntent,
                                           ShareContent shareContent, boolean appendShareSource,
                                           boolean isAds) {
    AndroidUtils.saveLastUseTime(packageName);
    AndroidUtils.updateStoryShareCount(1);
    if (isAds) {
      return new AdsShareHelper(packageName, activity, sendIntent, shareContent, appendShareSource);
    }
    ShareApplication shareApplication = ShareApplication.fromName(packageName);
    if (shareApplication == null) {
      return new DefaultShareHelper(packageName, activity, sendIntent,
          shareContent, appendShareSource);
    }
    switch (shareApplication) {
      case FACEBOOK_APP_PACKAGE:
        return new FacebookShareHelper(packageName, activity, sendIntent,
            shareContent, appendShareSource);

      case GMAIL_APP_PACKAGE:
        return new GmailShareHelper(packageName, activity, sendIntent,
            shareContent, appendShareSource);

      case TWITTER_APP_PACKAGE:
        return new TwitterShareHelper(packageName, activity, sendIntent,
            shareContent, appendShareSource);

      case SMS_PACKAGE:
        if (null != shareContent) {
          return new SmsShareHelper(packageName, activity, sendIntent,
              shareContent, appendShareSource);
        }

      case WHATS_APP_PACKAGE:
        if (null != shareContent) {
          return new WhatsappShareHelper(packageName, activity, sendIntent,
              shareContent, appendShareSource);
        }
    }
    return new DefaultShareHelper(packageName, activity, sendIntent,
        shareContent, appendShareSource);
  }

  private static class FacebookShareHelper extends ShareHelper {

    FacebookShareHelper(String packageName, Activity activity,
                        Intent sendIntent, ShareContent shareContent, boolean appendShareSource) {
      super(packageName, activity, sendIntent, shareContent, appendShareSource);
    }

    @Override
    public void share() {
      if (AndroidUtils.isAppInstalled(packageName) ||
          !AndroidUtils.isAppDisabled(packageName)) {
        if (null == shareContent) {
          sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForApplication(
              CommonUtils.getString(R.string.share_app_text),
              ShareUTMHelper.getShareURL(AndroidUtils.getAppendedShareUrl(Constants.SHARE_APP_DEFAULT),
                  packageName), true));
          startShareActivity();
        } else {
          sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForContent(
              HtmlContentHelper.getShareDescription(shareContent.getContent()), shareContent,
              packageName, appendShareSource));
          if (shareContent.getFileUri() != null &&
              !CommonUtils.isEmpty(shareContent.getFileUri().toString())) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, shareContent.getFileUri());
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.setType(Constants.INTENT_TYPE_IMAGE);
          }
          startShareActivityForResult();
        }
      } else {
        FontHelper.showCustomFontToast(activity,
            activity.getString(R.string.facebook_share_error), Toast.LENGTH_SHORT);
      }
    }
  }

  private static class GmailShareHelper extends ShareHelper {

    GmailShareHelper(String packageName, Activity activity,
                     Intent sendIntent, ShareContent shareContent, boolean appendShareSource) {
      super(packageName, activity, sendIntent, shareContent, appendShareSource);
    }

    @Override
    public void share() {
      if (!AndroidUtils.isAppDisabled(packageName)) {
        if (null == shareContent) {
          sendIntent.putExtra(Intent.EXTRA_SUBJECT, String.valueOf(android.text.Html.fromHtml(
              CommonUtils.getString(R.string.app_share_email_subject))));
          sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForApplication(
              CommonUtils.getString(R.string.share_app_text),
              ShareUTMHelper.getShareURL(AndroidUtils.getAppendedShareUrl(Constants.SHARE_APP_DEFAULT),
                  packageName), true));
          startShareActivityForResult();
        } else {
          if (shareContent.getContent() == null) {
            shareContent.setContent(Constants.EMPTY_STRING);
          }
          sendIntent.putExtra(Intent.EXTRA_SUBJECT, shareContent.getSubject());
          sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForContent(
              shareContent.getTitle(), shareContent, packageName, true));
          if (shareContent.getFileUri() != null &&
              !CommonUtils.isEmpty(shareContent.getFileUri().toString())) {
            sendIntent.putExtra(Intent.EXTRA_STREAM, shareContent.getFileUri());
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.setType(Constants.INTENT_TYPE_IMAGE);
          }
          startShareActivityForResult();
        }
      } else {
        FontHelper.showCustomFontToast(activity,
            activity.getString(R.string.gmail_share_error), Toast.LENGTH_SHORT);
      }
    }
  }

  private static class TwitterShareHelper extends ShareHelper {

    TwitterShareHelper(String packageName, Activity activity,
                       Intent sendIntent, ShareContent shareContent, boolean appendShareSource) {
      super(packageName, activity, sendIntent, shareContent, appendShareSource);
    }

    @Override
    public void share() {
      if (AndroidUtils.isAppInstalled(packageName)) {
        if (null == shareContent) {
          sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForApplication(
              CommonUtils.getString(R.string.share_source), ShareUTMHelper.getShareURL(
                  AndroidUtils.getAppendedShareUrl(Constants.SHARE_APP_DEFAULT), packageName),
              true));
          startShareActivity();
        } else {
          String source;
          String langTitle;
          if (!CommonUtils.isEmpty(shareContent.getLangTitles())) {
            langTitle = shareContent.getLangTitles().toLowerCase();
          } else {
            langTitle = Constants.EMPTY_STRING;
          }
          // to set appropriate language share message.
          switch (langTitle) {
            case Constants.MARATHI_LANGUAGE_CODE:
              source = CommonUtils.getString(R.string.share_source_twitter_mr);
              break;
            case Constants.HINDI_LANGUAGE_CODE:
              source = CommonUtils.getString(R.string.share_source_twitter_hi);
              break;
            case Constants.GUJRATI_LANGUAGE_CODE:
              source = CommonUtils.getString(R.string.share_source_twitter_gu);
              break;
            case Constants.KANNADA_LANGUAGE_CODE:
              source = CommonUtils.getString(R.string.share_source_twitter_kn);
              break;
            case Constants.MALYALAM_LANGUAGE_CODE:
              source = CommonUtils.getString(R.string.share_source_twitter_ml);
              break;
            case Constants.BENGALI_LANGUAGE_CODE:
              source = CommonUtils.getString(R.string.share_source_twitter_bn);
              break;
            default:
              source = CommonUtils.getString(R.string.share_source_twitter);
          }

          String title = shareContent.getTitle();

          // 2 new line char. Before and after share url.
          int sharableTextSize = shareContent.getShareUrl().length() + 2 + source.length();
          int remainingSpaceLeft = Constants.SHARE_DESCRIPTION_SIZE - sharableTextSize;

          // if text length exceeds 140 character then shorten title.
          if (remainingSpaceLeft > 0) {
            if (title.length() >= remainingSpaceLeft) {
              //shorten the title. One less size for ellipsis char.
              title = title.substring(0, remainingSpaceLeft - 2) + CommonUtils
                  .getString(R.string.ellipsis_char);
            }
          } else {
            title = Constants.EMPTY_STRING;
          }

          String sharableText = getExtraTextForContent(title, shareContent, packageName, true);
          sendIntent.putExtra(Intent.EXTRA_TEXT, sharableText);
          startShareActivityForResult();
        }
      } else {
        FontHelper.showCustomFontToast(activity,
            activity.getString(R.string.twitter_share_error), Toast.LENGTH_SHORT);
      }
    }
  }

  private static class SmsShareHelper extends ShareHelper {

    SmsShareHelper(String packageName, Activity activity,
                   Intent sendIntent, ShareContent shareContent, boolean appendShareSource) {
      super(packageName, activity, sendIntent, shareContent, appendShareSource);
    }

    @Override
    public void share() {
      sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForContent(
          HtmlContentHelper.getShareDescription(shareContent.getContent()), shareContent,
          packageName, true));
      startShareActivityForResult();
    }
  }

  private static class WhatsappShareHelper extends ShareHelper {

    WhatsappShareHelper(String packageName, Activity activity,
                        Intent sendIntent, ShareContent shareContent, boolean appendShareSource) {
      super(packageName, activity, sendIntent, shareContent, appendShareSource);
    }

    @Override
    public void share() {
      if (AndroidUtils.isAppInstalled(packageName)) {
        sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForContent(
            shareContent.getTitle(), shareContent, packageName, true));
        if (shareContent.getFileUri() != null &&
            !CommonUtils.isEmpty(shareContent.getFileUri().toString())) {
          sendIntent.putExtra(Intent.EXTRA_STREAM, shareContent.getFileUri());
          sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          sendIntent.setType(Constants.INTENT_TYPE_IMAGE);
        }
        startShareActivityForResult();
      } else {
        FontHelper.showCustomFontToast(activity,
            activity.getString(R.string.whatsapp_share_error), Toast.LENGTH_SHORT);
      }
    }
  }

  private static class DefaultShareHelper extends ShareHelper {

    DefaultShareHelper(String packageName, Activity activity,
                       Intent sendIntent, ShareContent shareContent, boolean appendShareSource) {
      super(packageName, activity, sendIntent, shareContent, appendShareSource);
    }

    @Override
    public void share() {
      if (null == shareContent) {
        sendIntent.putExtra(Intent.EXTRA_SUBJECT,
            CommonUtils.getString(R.string.app_share_email_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT,
            getExtraTextForApplication(CommonUtils.getString(R.string.share_app_text),
                ShareUTMHelper.getShareURL(AndroidUtils.getAppendedShareUrl(Constants.SHARE_APP_DEFAULT),
                    packageName), true));
        startShareActivity();
      } else {
        if (!CommonUtils.isEmpty(shareContent.getSubject())) {
          sendIntent.putExtra(Intent.EXTRA_SUBJECT, shareContent.getSubject());
        }
        sendIntent.putExtra(Intent.EXTRA_TEXT, getExtraTextForContent(
            shareContent.getTitle(), shareContent, packageName, true));
        if (shareContent.getFileUri() != null &&
            !CommonUtils.isEmpty(shareContent.getFileUri().toString())) {
          sendIntent.putExtra(Intent.EXTRA_STREAM, shareContent.getFileUri());
          sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          sendIntent.setType(Constants.INTENT_TYPE_IMAGE);
        }
        startShareActivityForResult();
      }
    }
  }

  private static class AdsShareHelper extends ShareHelper {

    AdsShareHelper(String packageName, Activity activity,
                   Intent sendIntent, ShareContent shareContent, boolean appendShareSource) {
      super(packageName, activity, sendIntent, shareContent, appendShareSource);
    }

    @Override
    public void share() {
      if (shareContent == null) {
        return;
      }
      if (!CommonUtils.isEmpty(shareContent.getSubject())) {
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, shareContent.getSubject());
      }
      sendIntent.putExtra(Intent.EXTRA_TEXT, shareContent.getTitle());
      if (shareContent.getFileUri() != null &&
          !CommonUtils.isEmpty(shareContent.getFileUri().toString())) {
        sendIntent.putExtra(Intent.EXTRA_STREAM, shareContent.getFileUri());
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType(Constants.INTENT_TYPE_IMAGE);
      }
      startShareActivityForResult();
    }
  }

  public static String getExtraTextForApplication(String app_share_text, String shareApp,
                                                   boolean appendShareSource) {
    String footerText = CommonUtils.getString(R.string.share_source);
    return String.valueOf(android.text.Html.fromHtml(app_share_text + Constants.ANCHER_OPEN_TAG +
        shareApp + Constants.CLOSE_TAG + shareApp + Constants.ANCHER_CLOSE_TAG) + footerText);
  }

  private static String getExtraTextForContent(String text, ShareContent shareContent,
                                               String packageName, boolean appendShareSource) {
    String footerText = null;
    if (appendShareSource) {
      footerText = getStoryShareFooterText(shareContent.getLangTitles(), shareContent.getSourceName());
    }
    if (footerText == null) {
      footerText = Constants.EMPTY_STRING;
    }

    String shareUrl = shareContent.getShareUrl();
    if (CommonUtils.isEmpty(shareUrl)) {
      shareUrl = Constants.SHARE_APP_DEFAULT;
    } else {
      shareUrl = Constants.ANCHER_OPEN_TAG +
          ShareUTMHelper.getShareURL(shareUrl, packageName) + Constants.CLOSE_TAG +
          ShareUTMHelper.getShareURL(AndroidUtils.getAppendedShareUrl(shareUrl), packageName) +
          Constants.ANCHER_CLOSE_TAG;
    }

    return String.valueOf(android.text.Html.fromHtml(
        (!CommonUtils.isEmpty(text) ? text : Constants.EMPTY_STRING) + shareUrl) + footerText);
  }

  public static String getStoryShareFooterText(String langTitles, String sourceName) {
    ShareTextMappingResponse response = JsonUtils.fromJson(
        PreferenceManager.getPreference(GenericAppStatePreference.SHARE_TEXT_MAPPING,
            Constants.EMPTY_STRING), ShareTextMappingResponse.class);
    if (response != null) {
      String shareTextMappingByLang = response.getShareTextMappingByLang(langTitles);
      if (langTitles == null || shareTextMappingByLang == null) {
        shareTextMappingByLang =
            response.getShareTextMappingByLang(AppUserPreferenceUtils.getUserNavigationLanguage());
      }
      return prefixSourceToFooter(shareTextMappingByLang, sourceName);
    }
    return Constants.EMPTY_STRING;
  }

  @Nullable
  private static String prefixSourceToFooter(@Nullable String footer, @Nullable String sourceName) {
    if (!PreferenceManager.getBoolean(Constants.INCLUDE_PUBLISHER_IN_SHARE_TEXT, false)) {
      Logger.v(TAG, "prefixSourceToFooter("+footer+", "+sourceName+") disabled in handshake");
      return footer;
    }
    if (sourceName == null) {
      Logger.v(TAG, "prefixSourceToFooter("+footer+", "+sourceName+")");
      return footer;
    }
    return new StringBuilder().append("Source : \"")
        .append(sourceName)
        .append("\"")
        .append(Constants.SPACE_STRING)
        .append(footer != null ? footer : Constants.EMPTY_STRING)
        .toString();
  }
}
