package com.newshunt.common.helper.common;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.language.Language;

import java.util.ArrayList;
import java.util.List;

/**
 * Class with very generic utility methods for Language checks
 *
 * @author vinod.bc
 */
public class LanguageUtils {
  private static final String URDU_CODE = "ur";
  private static final int ASCII_CHARACTERS_LIMIT = 128;

  public static Boolean isUrdu(String langCode) {
    if (CommonUtils.isEmpty(langCode)) {
      return false;
    }

    return langCode.equalsIgnoreCase(URDU_CODE);
  }

  /**
   * Identify the character set contains unicode characters.
   *
   * @param characterSet Character set to check.
   * @return True if character set has only ASCII characters.
   */
  public static boolean isASCIICharacters(String characterSet) {
    for (char c : characterSet.toCharArray()) {
      if (c >= ASCII_CHARACTERS_LIMIT) {
        return false;
      }
    }
    return true;
  }

  public static String getMatchingLangCode(String langName, List<Language> languageList) {
    String matchingLangCode = null;

    for(Language language : languageList) {
      if (CommonUtils.equalsIgnoreCase(language.getName(), langName)) {
        matchingLangCode = language.getCode();
        break;
      }
    }
    return matchingLangCode;
  }

  public static Language getMatchingLanguage(String langCode, List<Language> languageList) {
    if (CommonUtils.isEmpty(langCode) || CommonUtils.isEmpty(languageList)) {
      return null;
    }

    for (Language language : languageList) {
      if (CommonUtils.equalsIgnoreCase(language.getCode(), langCode)) {
        return language;
      }
    }
    return null;
  }

  public static List<Language> getLanguageList(List<String> langCodes, List<Language> existingLangList){
    if (CommonUtils.isEmpty(langCodes) || CommonUtils.isEmpty(existingLangList)) {
      return null;
    }
    List<Language> langList = new ArrayList<>();
    for(String langCode: langCodes){
      langList.add(getMatchingLanguage(langCode,existingLangList));
    }
    return langList;
  }
}
