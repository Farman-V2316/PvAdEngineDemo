/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

import android.util.Base64;
import android.util.Pair;

import androidx.collection.LruCache;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Password encryption utility shared by server team
 *
 * @author karthik.r
 */
public class PasswordEncryption {
  private static final String LOG_TAG = "PasswordEncryption";
  private static final String ENCODED_PUBLIC_KEY =
      "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlHZk1BMEdDU3FHU0liM0RRRUJBUVVBQTRHTkFEQ0JpUUtCZ1FDWkd2L1dncXo1OEhjOURMZUFObHRmNFVJMwp3Vkoyb0xJYzZEemRheFdaTXRSVkYzTGYySlFSZURQMTRsKzNYUGdXaC9lVlFBU1Z3QTZKaUYxemw1WXhJS1ovCmppc2NEaFRDS25idmE1dTdrYjFMb0FUTlFKQlZtZGhSOFVZbWc3dUZWMnJTVUg3UEJ3VytyLzhKcm9sOXA1SGEKckIxVlBPMEtIR0YyZ2JNS2V3SURBUUFCCi0tLS0tRU5EIFBVQkxJQyBLRVktLS0tLQo=";

  private static final String PUBLIC_KEY_VERSION = "1";
  private static final String DELIMITER = "|";
  private static Pair<PublicKey, String> latestKeyversion;
  private static Pair<PublicKey, String> latestAdsKeyversion;
  private static SecretKey secretKey;
  private static Cipher adsAESCipher, eESCipher;
  private static String encodedEncryptedAdsKey, encodedEncryptedKey;
  private static IvParameterSpec ivParameterSpec;

  static {
    // Frame the first key here
    reloadKeyAndVersion();
    reloadAdsKeyAndVersion();
    generateAESKey();
  }

  private static void generateAESKey() {
    KeyGenerator keyGen = null;
    try {
      keyGen = KeyGenerator.getInstance("AES");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    SecureRandom random = new SecureRandom(); // cryptograph. secure random
    keyGen.init(128, random);
    secretKey = keyGen.generateKey();
    ivParameterSpec = new IvParameterSpec(new byte[] {0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0});

    try {
      adsAESCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      adsAESCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
      Cipher adsCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      adsCipher.init(Cipher.ENCRYPT_MODE, latestAdsKeyversion.first);
      encodedEncryptedAdsKey = Base64.encodeToString(adsCipher.doFinal(secretKey.getEncoded()),
          Base64.NO_WRAP);

      eESCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      eESCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
      Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
      cipher.init(Cipher.ENCRYPT_MODE, latestKeyversion.first);
      encodedEncryptedKey = Base64.encodeToString(cipher.doFinal(secretKey.getEncoded()),
          Base64.NO_WRAP);
    } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidAlgorithmParameterException e) {
      e.printStackTrace();
    }
  }

    public static void reloadKeyAndVersion() {
      latestKeyversion = null;
      String encodedKey =
              PreferenceManager.getPreference(AppStatePreference.PUBLIC_KEY, ENCODED_PUBLIC_KEY);
      String version =
              PreferenceManager.getPreference(AppStatePreference.PUBLIC_KEY_VERSION, PUBLIC_KEY_VERSION);
      try {
        latestKeyversion = getLatestKeyVersionPair(encodedKey, version);
        if (latestKeyversion != null) {
          boolean hasPref = PreferenceManager.containsPreference(AppStatePreference.PUBLIC_KEY);
          Logger.d(LOG_TAG, String.format("reloadKeyAndVersion: loaded from prefs. hasPref?%s, key=%s", hasPref, latestKeyversion));
        }
      } catch (Throwable throwable) {
        Logger.caughtException(throwable);
      }
      if (latestKeyversion == null) {
        latestKeyversion = getLatestKeyVersionPair(ENCODED_PUBLIC_KEY, PUBLIC_KEY_VERSION);
        Logger.e(LOG_TAG, "reloadKeyAndVersion:: loaded from defaults  "+latestKeyversion);
        if (PasswordEncryptionUtil.getLogger() != null) {
          PasswordEncryptionUtil.getLogger().logEvent(
                  "reloadKeyAndVersion: invalid key in prefs", encodedKey, version);
        }
      }
    }

  private static void reloadAdsKeyAndVersion() {
    latestAdsKeyversion = null;
    String key = PreferenceManager.getPreference(AppStatePreference.PUBLIC_ADS_KEY, ENCODED_PUBLIC_KEY);
    String version = PreferenceManager.getPreference(AppStatePreference.PUBLIC_ADS_KEY_VERSION, PUBLIC_KEY_VERSION);
    try {
      latestAdsKeyversion = getLatestAdsKeyVersionPair(key, version);
      if (latestAdsKeyversion != null) {
        boolean hasPref = PreferenceManager.containsPreference(AppStatePreference.PUBLIC_ADS_KEY);
        Logger.d(LOG_TAG, String.format("reloadAdsKeyAndVersion: loaded from prefs. hasPref?%s, key=%s", hasPref, latestAdsKeyversion));
      }
    } catch (Throwable throwable) {
      Logger.caughtException(throwable);
    }
    if (latestAdsKeyversion == null) {
      latestAdsKeyversion = getLatestAdsKeyVersionPair(ENCODED_PUBLIC_KEY, PUBLIC_KEY_VERSION);
      Logger.e(LOG_TAG, "reloadAdsKeyAndVersion: loaded from defaults "+latestAdsKeyversion);
      if (PasswordEncryptionUtil.getLogger() != null) {
        PasswordEncryptionUtil.getLogger().logEvent(
                "reloadAdsKeyAndVersion: invalid key in prefs", key, version);
      }
    }
  }

  public static boolean validateAndPersistForAds(String encodedKey, String version) {
    try {
      Pair<PublicKey, String> keyAndVersion = getLatestAdsKeyVersionPair(encodedKey, version);
      if (keyAndVersion != null) {
        latestAdsKeyversion = keyAndVersion;
        PreferenceManager.savePreference(AppStatePreference.PUBLIC_ADS_KEY, encodedKey);
        PreferenceManager.savePreference(AppStatePreference.PUBLIC_ADS_KEY_VERSION, version);
        return true;
      }
    } catch (Throwable throwable) {
      Logger.caughtException(throwable);
    }
    return false;
  }

  public static boolean validateAndPersist(String encodedKey, String version) {
    try {
      Pair<PublicKey, String> keyAndVersion = getLatestKeyVersionPair(encodedKey, version);
      if (keyAndVersion != null) {
        latestKeyversion = keyAndVersion;
        PreferenceManager.savePreference(AppStatePreference.PUBLIC_KEY, encodedKey);
        PreferenceManager.savePreference(AppStatePreference.PUBLIC_KEY_VERSION, version);
        return true;
      }
    } catch (Throwable throwable) {
      Logger.caughtException(throwable);
    }
    return false;
  }

  /**
   * encrypt password for sending
   *
   * @param strToEncrypt - string to be encrypted
   * @return - encrypted string
   * @throws Exception
   */
  private static synchronized String _encrypt(String strToEncrypt) throws Exception {
    // Take a copy and use, so we are resilient to key change from some other thread.
    Pair<PublicKey, String> localCopyOfKey = latestKeyversion;
    if (localCopyOfKey == null) {
      // If not able to key get, just use plain text
      return strToEncrypt;
    }

    String encryptedMessage =
        Base64.encodeToString(eESCipher.doFinal(strToEncrypt.getBytes(Charset.forName("UTF-8"))),
            Base64.NO_WRAP);

    return encryptedMessage + DELIMITER + encodedEncryptedKey + DELIMITER + localCopyOfKey.second;
  }

  /**
   * @param strToEncrypt - string to be encrypted
   * @return - encrypted string
   * @throws Exception
   */
  private static synchronized String _encryptForAds(String strToEncrypt) throws Exception {
    // Take a copy and use, so we are resilient to key change from some other thread.
    Pair<PublicKey, String> localCopyOfKey = latestAdsKeyversion;
    if (localCopyOfKey == null) {
      // If not able to key get, just use plain text
      return strToEncrypt;
    }

    String encryptedMessage =
        Base64.encodeToString(adsAESCipher.doFinal(strToEncrypt.getBytes(Charset.forName("UTF-8"))),
            Base64.NO_WRAP);
    return encryptedMessage + DELIMITER + encodedEncryptedAdsKey + DELIMITER + localCopyOfKey.second;
  }

  private static final LruCache<String, String> encMap = new LruCache<>(16);

  public static String encrypt(String strToEncrypt) throws Exception {
    return encrypt(strToEncrypt, true);
  }

  public static String encrypt(String strToEnc, boolean canCache) throws Exception {
    Logger.d(LOG_TAG, "encrypt: mapsize=" + encMap);
    if(!canCache) return _encrypt(strToEnc);
    String encdStr = encMap.get(strToEnc);
    if(!CommonUtils.isEmpty(encdStr)) return encdStr;
    encdStr = _encrypt(strToEnc);
    encMap.put(strToEnc, encdStr);
    return encdStr;
  }

  private static final LruCache<String, String> encAdMap = new LruCache<>(16);

  public static String encryptForAds(String strToEnc) throws Exception {
    Logger.d(LOG_TAG, "encryptForAds: mapsize="+encAdMap);
    String encdStr = encAdMap.get(strToEnc);
    if(!CommonUtils.isEmpty(encdStr)) return encdStr;
    encdStr = _encryptForAds(strToEnc);
    encAdMap.put(strToEnc, encdStr);
    return encdStr;
  }
  /**
   * encrypt String for sending
   *
   * @param bytesToEncrypt - string to be encrypted
   * @return - encrypted string
   * @throws Exception
   */
  public static String encryptForAds(byte[] bytesToEncrypt) throws Exception {
    // Take a copy and use, so we are resilient to key change from some other thread.
    Pair<PublicKey, String> localCopyOfKey = latestKeyversion;
    if (localCopyOfKey == null) {
      // If not able to key get, just use plain text
      return new String(bytesToEncrypt);
    }

    String encryptedMessage =
        Base64.encodeToString(adsAESCipher.doFinal(bytesToEncrypt), Base64.NO_WRAP);
    return encryptedMessage + DELIMITER + encodedEncryptedAdsKey + DELIMITER + localCopyOfKey.second;
  }


  /**
   * encrypt String for sending
   *
   * @param bytesToEncrypt - string to be encrypted
   * @return - encrypted string
   * @throws Exception
   */
  public static String encrypt(byte[] bytesToEncrypt) throws Exception {
    // Take a copy and use, so we are resilient to key change from some other thread.
    Pair<PublicKey, String> localCopyOfKey = latestKeyversion;
    if (localCopyOfKey == null) {
      // If not able to key get, just use plain text
      return new String(bytesToEncrypt);
    }

    String encryptedMessage =
        Base64.encodeToString(eESCipher.doFinal(bytesToEncrypt), Base64.NO_WRAP);
    return encryptedMessage + DELIMITER + encodedEncryptedKey + DELIMITER + localCopyOfKey.second;
  }

  private static Pair<PublicKey, String> getLatestKeyVersionPair(String encodedKey, String version) {
    String decodedKey = new String(Base64.decode(encodedKey, Base64.DEFAULT));
    decodedKey = decodedKey.replace("-----BEGIN PUBLIC KEY-----\n", "");
    decodedKey = decodedKey.replace("-----END PUBLIC KEY-----", "");
    byte[] backKey = Base64.decode(decodedKey, Base64.DEFAULT);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(backKey);
    KeyFactory kf = null;
    try {
      kf = KeyFactory.getInstance("RSA");
      PublicKey key = kf.generatePublic(spec);
      return new Pair<>(key, version);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      Logger.caughtException(e);
    }

    return null;
  }

  private static Pair<PublicKey, String> getLatestAdsKeyVersionPair(String encodedKey, String version) {
    String decodedKey = new String(Base64.decode(encodedKey, Base64.DEFAULT));
    decodedKey = decodedKey.replace("-----BEGIN PUBLIC KEY-----\n", "");
    decodedKey = decodedKey.replace("-----END PUBLIC KEY-----", "");
    byte[] backKey = Base64.decode(decodedKey, Base64.DEFAULT);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(backKey);
    KeyFactory kf = null;
    try {
      kf = KeyFactory.getInstance("RSA");
      PublicKey key = kf.generatePublic(spec);
      return new Pair<>(key, version);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      Logger.caughtException(e);
    }

    return null;
  }

  public static String encryptWithDeviceKey(String plainTextString) {
    String key =
        PreferenceManager.getPreference(AppStatePreference.DEVICE_UNIQUE_KEY, Constants.EMPTY_STRING);

    if (CommonUtils.isEmpty(key)) {
      KeyGenerator keyGen = null;
      try {
        keyGen = KeyGenerator.getInstance("AES");
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }

      SecureRandom random = new SecureRandom(); // cryptograph. secure random
      keyGen.init(128, random);
      SecretKey deviceSpecificKey = keyGen.generateKey();
      key = Base64.encodeToString(deviceSpecificKey.getEncoded(), Base64.DEFAULT);
      PreferenceManager.savePreference(AppStatePreference.DEVICE_UNIQUE_KEY, key);
    }

    try {
      SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES");
      IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.decode(key, Base64.DEFAULT));
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
      return Base64.encodeToString(cipher.doFinal(plainTextString.getBytes(Charset.forName("UTF-8"))),
          Base64.NO_WRAP);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException |
        InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException |
        IllegalBlockSizeException e) {
      Logger.caughtException(e);
    }

    return plainTextString;
  }

  public static String decryptWithDeviceKey(String encryptedString) {
    if (CommonUtils.isEmpty(encryptedString)) {
      return Constants.EMPTY_STRING;
    }

    String key =
        PreferenceManager.getPreference(AppStatePreference.DEVICE_UNIQUE_KEY, Constants.EMPTY_STRING);

    if (CommonUtils.isEmpty(key)) {
      // Looks like so far its not encrypted.
      return encryptedString;
    }

    try {
      SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES");
      IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.decode(key, Base64.DEFAULT));
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
      return new String(cipher.doFinal(Base64.decode(encryptedString, Base64.NO_WRAP)),
          Charset.forName("UTF-8"));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException |
        BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException |
        InvalidKeyException e) {
      PreferenceManager.remove(AppStatePreference.DEVICE_UNIQUE_KEY);
      Logger.caughtException(e);
    }

    // Decrypt here
    return encryptedString;
  }
}

