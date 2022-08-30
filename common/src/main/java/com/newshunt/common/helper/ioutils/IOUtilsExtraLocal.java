/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.ioutils;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Local storage encryption utilities
 *
 * @author neeraj.mittal
 */
public class IOUtilsExtraLocal {
  public byte[] encryptDataWithTransferKey(byte[] aDataToEncrypt, @NonNull String clientId,
                                           @NonNull String uDID) throws Exception {
    IOUtilsExtra iOUtilsExtra = new IOUtilsExtra();
    SecretKeySpec secretKey = new SecretKeySpec(iOUtilsExtra.getTransferKeyWithDigest(
        clientId, uDID), IOUtilsExtraHelper.SYMMETRIC_ALGORITHM);
    Cipher cipher = Cipher.getInstance(IOUtilsExtraHelper.SYMMETRIC_CIPHER);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    AlgorithmParameters params = cipher.getParameters();
    byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
    byte[] encryptedData = cipher.doFinal(aDataToEncrypt);
    cipher = null;
    aDataToEncrypt = null;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(iv);
    byteArrayOutputStream.write(encryptedData);
    return byteArrayOutputStream.toByteArray();
  }

  // // AMIT: commented on purpose - needs to be uncommented whenever we want
  // to change JS
  // public byte[] encryptDataWithCustomKey(byte[] aDataToEncrypt)
  // throws Exception {
  // IOUtilsExtra iOUtilsExtra = new IOUtilsExtra();
  // SecretKeySpec secretKey = new SecretKeySpec(
  // iOUtilsExtra.getTransferKeyWithDigest("786957341212",
  // "89723108972190372903"),
  // IOUtilsExtraHelper.SYMMETRIC_ALGORITHM);
  //
  // Cipher cipher = Cipher.getInstance(IOUtilsExtraHelper.SYMMETRIC_CIPHER);
  // cipher.init(Cipher.ENCRYPT_MODE, secretKey);
  //
  // AlgorithmParameters params = cipher.getParameters();
  // byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
  // byte[] encryptedData = cipher.doFinal(aDataToEncrypt);
  //
  // ByteArrayOutputStream byteArrayOutputStream = new
  // ByteArrayOutputStream();
  // byteArrayOutputStream.write(iv);
  // byteArrayOutputStream.write(encryptedData);
  // cipher = null;
  // return byteArrayOutputStream.toByteArray();
  //
  // }

  public byte[] encryptDeviceData(byte[] aDataToEncrypt, @NonNull String clientId,
                                  @NonNull String uDID) throws Exception {
    SecretKeySpec secretKey = new SecretKeySpec(getDeviceDataKeyWithDigest(
        clientId, uDID), IOUtilsExtraHelper.SYMMETRIC_ALGORITHM);
    Cipher cipher = Cipher.getInstance(IOUtilsExtraHelper.SYMMETRIC_CIPHER);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    AlgorithmParameters params = cipher.getParameters();
    byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
    byte[] encryptedData = cipher.doFinal(aDataToEncrypt);
    cipher = null;
    aDataToEncrypt = null;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(iv);
    byteArrayOutputStream.write(encryptedData);
    return byteArrayOutputStream.toByteArray();
  }

  // // AMIT: commented on purpose - needs to be uncommented whenever we want
  // // to verify encryption
  // public byte[] decryptDeviceData(byte[] data)
  // throws InvalidKeyException, IllegalBlockSizeException,
  // BadPaddingException, UnsupportedEncodingException,
  // NoSuchAlgorithmException, NoSuchPaddingException,
  // InvalidAlgorithmParameterException, InvalidParameterSpecException {
  // SecretKeySpec secretKey = new SecretKeySpec(getDeviceDataKeyWithDigest(
  // NewsHuntAppController.clientId, Splash.uuid),
  // IOUtilsExtraHelper.SYMMETRIC_ALGORITHM);
  // IvParameterSpec spec = new IvParameterSpec(IOUtilsExtra.copyOfRange(data,
  // 0, 16));
  // Cipher cipher = Cipher.getInstance(IOUtilsExtraHelper.SYMMETRIC_CIPHER);
  // cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
  // byte[] decryptedData = cipher
  // .doFinal(IOUtilsExtra.copyOfRange(data, 16, data.length));
  // cipher = null;
  // data = null;
  // return decryptedData;
  // }

  private byte[] getDeviceDataKeyWithDigest(String clientId, String udid)
      throws NoSuchAlgorithmException {
    String secretKey = "nksdkj089&23mn!$&*kdfd^*^ZCXDnkxznflkjc{:'></]{jcxns-+ak~`2sdjfi"; // dummy
    StringBuilder transferKey = new StringBuilder();
    transferKey.append(IOUtilsExtra.substring(secretKey, 0, 5))
        .append(IOUtilsExtra.substring(clientId, 0, 2))
        .append(IOUtilsExtra.substring(secretKey, 5, 9))
        .append(IOUtilsExtra.substring(secretKey, 34, 41))
        .append(IOUtilsExtra.substring(secretKey, 9, 16))
        .append(IOUtilsExtra.substring(udid, 6, 13))
        .append(IOUtilsExtra.substring(clientId, 2, 6))
        .append(IOUtilsExtra.substring(secretKey, 16, 21))
        .append(IOUtilsExtra.substring(clientId, 6, 10))
        .append(IOUtilsExtra.substring(udid, 6, 13))
        .append(IOUtilsExtra.substring(udid, 2, 6))
        .append(IOUtilsExtra.substring(secretKey, 21, 34))
        .append(IOUtilsExtra.substring(udid, 13))
        .append(IOUtilsExtra.substring(clientId, 10))
        .append(IOUtilsExtra.substring(secretKey, 41));

    // hasing transferKey
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(transferKey.toString().getBytes());

    return md.digest();
  }
}
