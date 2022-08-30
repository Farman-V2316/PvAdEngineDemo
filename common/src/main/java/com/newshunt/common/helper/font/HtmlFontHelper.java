/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.font;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;

/**
 * Helper class to add the relevant html tags and adding font tags
 *
 * @author maruti.borker
 */
public class HtmlFontHelper {
  //TODO(maruti.borker): Read this from a file and use a substitution pattern
  private static String HTML_BEGIN = "<html><head>";
  private static String HTML_MIDDLE =
      "</head><body leftmargin=\"0\" topmargin=\"0\" rightmargin=\"0\" bottommargin=\"0\">";
  private static String HTML_MIDDLE_14 = "\"font-14 ";
  private static String HTML_MIDDLE_16 = "\"font-16 ";
  private static String HTML_MIDDLE_18 = "\"font-18 ";
  private static String HTML_MIDDLE_20 = "\"font-20 ";
  private static String HTML_MIDDLE_URDU = "</head><body leftmargin=\"0\" topmargin=\"0\"" +
      " rightmargin=\"0\" bottommargin=\"0\" dir=\"RTL\" text-align=\"right\" >";
  private static String HTML_MIDDLE_LEFT="</head><body class=";
  private static String HTML_MIDDLE_RIGHT="\"leftmargin=\"0\" topmargin=\"0\" rightmargin=\"0\" bottommargin=\"0\">";

  private static String HTML_END = "</body></html>";
  private static String HTML_BODY_CLOSE = "</body>";
  private static String HTML_CLOSE = "</html>";
  private static String STYLE_BASIC_WITH_COPY_PASTE_DISABLED =
      "<style>* {margin-left:0;margin-right:0;-webkit-user-select: none;}</style>";
  private static String STYLE_BASIC =
      "<style>* {margin-left:0;margin-right:0;}</style>";

  private static String STYLE_INDIC_FONT =
      "<style>@font-face {font-family: 'newshunt-regular';src:" +
          " url('file:///android_asset/fonts/newshunt-regular.otf');font-weight: normal;" +
          "font-style: normal; } body {font-family: 'newshunt-regular';line-height: 150%}" +
          " @font-face {font-family: 'newshunt-bold';src: " +
          "url('file:///android_asset/fonts/newshunt-bold.otf');font-weight: bold;font-style: " +
          "normal;} " +
          "b {font-family: 'newshunt-bold'; font-style:bold; line-height: 150%} </style>";
  private static String STYLE_CUSTOM_FONT = "<style>@font-face {font-family: 'Custom_fonts';src:" +
      " url(%1$s_normal);font-weight: normal;" +
      "font-style: normal; } body {font-family: 'Custom_fonts';line-height: %2$s}" +
      "</style>";

  private static String LINE_HEIGHT = "150%";

  private static String STYLE_MINION_FONT = "<style>@font-face {font-family: 'Noto Sans';font-weight: normal;" +
      "font-style: normal;line-height: 150%; }body {font-family: 'Noto Sans'; line-height: 150%} " +
      "</style>";
  private static String STYLE_DARK = "<style> body{ background: #202020; color: #FFFFFF;} a {color: #4B9EFF;}</style>";
  private static String STYLE_DAY = "<style> a { color: #405DE6;}</style>";
  private static String META_TAGS = "<meta charset=\"UTF-8\"><meta name=\"viewport\"" +
      " content=\"width=device-width, initial-scale = 1.0, user-scalable=no\" />";

  public static String wrapToFontHTML(boolean isSelectAndCopyPasteDisabled, String data, String css,
                                      String js, boolean isUrdu, boolean isNightMode, String languageCode,int webViewFontSize) {
    if (DataUtil.isEmpty(data)) {
      return data;
    }
    FEOutput fontEngineOutput = FontHelper.convertToFontIndices(data);
    boolean isIndic = fontEngineOutput.isSupportedLanguageFound();
    String htmlMiddle;
    String htmlMiddleCenter = HTML_MIDDLE_16;

    if(webViewFontSize ==14){
      htmlMiddleCenter = HTML_MIDDLE_14;
    }else if(webViewFontSize ==16){
      htmlMiddleCenter = HTML_MIDDLE_16;
    }else if(webViewFontSize ==18){
      htmlMiddleCenter = HTML_MIDDLE_18;
    }else if(webViewFontSize ==20){
      htmlMiddleCenter = HTML_MIDDLE_20;
    }

    htmlMiddle = HTML_MIDDLE_LEFT+htmlMiddleCenter+languageCode+HTML_MIDDLE_RIGHT;
    Logger.d("HtmlFontHelper","htmlMiddle= " + htmlMiddle);

    if (isUrdu) {
      htmlMiddle = HTML_MIDDLE_URDU;
    }

    String styleTags = STYLE_BASIC;
    if (isSelectAndCopyPasteDisabled || isIndic) {
      styleTags = STYLE_BASIC_WITH_COPY_PASTE_DISABLED;
    }
    if (isIndic) {
      data = fontEngineOutput.getFontIndicesString().toString();
      styleTags += STYLE_INDIC_FONT;
    } else {
      styleTags += String.format(STYLE_CUSTOM_FONT, FontHelper.getFontName(languageCode), LINE_HEIGHT);
    }

    if (isNightMode) {
      styleTags += STYLE_DARK;
    }
    else {
      styleTags += STYLE_DAY;
    }

    String styleBasics = STYLE_BASIC;
    if (isSelectAndCopyPasteDisabled || isIndic) {
      styleBasics = STYLE_BASIC_WITH_COPY_PASTE_DISABLED;
    }
    return HTML_BEGIN + styleBasics + styleTags + css + META_TAGS + htmlMiddle + data
        + HTML_BODY_CLOSE + js + HTML_CLOSE;
  }

  public static String wrapDataWithHTML(String data) {

    //to handle cases which have html tags already
    if (data.contains("<html>")) {
      return data;
    }

    return HTML_BEGIN + STYLE_BASIC_WITH_COPY_PASTE_DISABLED + META_TAGS + HTML_MIDDLE_16 + data +
        HTML_END;
  }

  public static String wrapToFontHTML(String data, boolean isUrdu, boolean isNightMode) {
    return wrapToFontHTML(true, data, Constants.EMPTY_STRING, Constants.EMPTY_STRING, isUrdu,
        isNightMode, null,0);
  }

  //TODO(maruti.borker): Refactor this to be usuable everywhere
  public static String getShareDescription(String description) {
    if (description.length() < Constants.SHARE_DESCRIPTION_SIZE) {
      return description;
    }
    int pIndex = description.indexOf("<p>", Constants.SHARE_DESCRIPTION_SIZE - 1);
    int brIndex = description.indexOf("<br>", Constants.SHARE_DESCRIPTION_SIZE - 1);
    if (pIndex == -1 && brIndex == -1) {
      return description;
    }
    if (pIndex != -1 && brIndex != -1) {
      pIndex = (pIndex < brIndex) ? pIndex : brIndex;
    } else {
      pIndex = (pIndex < 0) ? brIndex : pIndex;
    }
    return description.substring(0, pIndex - 1);
  }

}