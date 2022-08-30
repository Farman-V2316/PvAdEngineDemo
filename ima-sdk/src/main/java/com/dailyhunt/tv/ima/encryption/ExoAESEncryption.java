package com.dailyhunt.tv.ima.encryption;

import android.util.Base64;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.preference.PreferenceManager;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Password encryption utility shared by server team
 *
 * @author ketki.garg
 */
public class ExoAESEncryption {
  private static final String KEY = "6a1lyhUN7#yU667V";
  private static final String KEY2 = "hUn7^5763aw_yU66";
  public static final String KEY_DH_STREAM_CONFIG1 = "KEY_DH_STREAM_CONFIG1";
  public static final String KEY_DH_STREAM_CONFIG2 = "KEY_DH_STREAM_CONFIG2";
  private static volatile SecretKeySpec secretKeySpec = null;
  private static volatile IvParameterSpec ivParameterSpec = null;

  private static SecretKeySpec getKey() throws Exception {
    String key = PreferenceManager.getString(KEY_DH_STREAM_CONFIG1);
    if (CommonUtils.isEmpty(key)) {
      key = KEY;
    }
    secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
    return secretKeySpec;
  }

  private static IvParameterSpec getKey1() throws Exception {
    String key = PreferenceManager.getString(KEY_DH_STREAM_CONFIG2);
    if (CommonUtils.isEmpty(key)) {
      key = KEY2;
    }
    ivParameterSpec = new IvParameterSpec(key.getBytes("UTF-8"));
    return ivParameterSpec;
  }

  /**
   * encrypt password for sending
   *
   * @param strToEncrypt - string to be encrypted
   * @return - encrypted string
   * @throws Exception
   */
  public static String encrypt(String strToEncrypt) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(Cipher.ENCRYPT_MODE, getKey(), getKey1());
    return Base64.encodeToString(cipher.doFinal(strToEncrypt.getBytes(Charset.forName("UTF-8"))),
        Base64.NO_WRAP);
  }

  /**
   * For decrypting password after receiving
   *
   * @param strToDecrypt - string to be decrypted
   * @return - decrypted string
   * @throws Exception
   */
  public static String decrypt(String strToDecrypt) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(Cipher.DECRYPT_MODE, getKey(), getKey1());
    return new String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.NO_WRAP)),
        Charset.forName("UTF-8"));
  }
}

