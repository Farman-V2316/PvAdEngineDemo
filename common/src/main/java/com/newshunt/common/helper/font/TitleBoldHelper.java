package com.newshunt.common.helper.font;

import main.java.com.newshunt.fontengine16bit.Intf;
import main.java.com.newshunt.fontengine16bit.Unicode2Ver16;


/**
 * @author amit.kankani
 *
 *
 */
public class TitleBoldHelper {

  public boolean shouldShowBoldTitle(String title) {
    Intf.LSCRIPT udtScript;
    for (int i = 0; i < title.length(); i++) {
      int val = title.charAt(i);
      Unicode2Ver16 unicode2Ver16 = new Unicode2Ver16();
      int langDetected = unicode2Ver16.FindLang(val);
      udtScript = Intf.LSCRIPT.values()[langDetected];
      switch (udtScript) {
        case BAN:
        case PUN:
        case GUJ:
        case TAM:
        case TEL:
        case KAN:
        case MAL:
        case ARA:
          return false;
        default:
          ;
      }
    }
    return true;
  }
}
