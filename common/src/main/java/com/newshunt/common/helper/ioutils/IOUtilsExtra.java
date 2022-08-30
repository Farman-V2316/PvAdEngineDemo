/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.ioutils;

import androidx.annotation.NonNull;
import android.util.Base64;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.FileUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.DeviceInfoHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * provides IO utility functions
 *
 * @author santosh.kulkarni
 */

public class IOUtilsExtra {

  private String key = null;
  private byte[] licKey = null;

  public static byte[] copyOfRange(byte[] data, int start, int end) {
    if (end > data.length) {
      end = data.length;
    }
    byte[] toReturn = new byte[end - start];
    for (int i = start; i < end; i++) {
      toReturn[i - start] = data[i];
    }
    return toReturn;
  }

  /**
   * <p>
   * Gets a substring from the specified String avoiding exceptions.
   * </p>
   * <p/>
   * <p>
   * A negative start position can be used to start <code>n</code> characters
   * from the end of the String.
   * </p>
   * <p/>
   * <p>
   * A <code>null</code> String will return <code>null</code>. An empty ("")
   * String will return "".
   * </p>
   * <p/>
   * <pre>
   * substring(null, *)   = null
   * substring("", *)     = ""
   * substring("abc", 0)  = "abc"
   * substring("abc", 2)  = "c"
   * substring("abc", 4)  = ""
   * substring("abc", -2) = "bc"
   * substring("abc", -4) = "abc"
   * </pre>
   *
   * @param str   the String to get the substring from, may be null
   * @param start the position to start from, negative means count back from the
   *              end of the String by this many characters
   * @return substring from start position, <code>null</code> if null String
   * input
   */
  public static String substring(String str, int start) {
    if (str == null) {
      return null;
    }

    return substring(str, start, str.length());
  }

  /**
   * <p>
   * Gets a substring from the specified String avoiding exceptions.
   * </p>
   * <p/>
   * <p>
   * A negative start position can be used to start/end <code>n</code>
   * characters from the end of the String.
   * </p>
   * <p/>
   * <p>
   * The returned substring starts with the character in the
   * <code>start</code> position and ends before the <code>end</code>
   * position. All position counting is zero-based -- i.e., to start at the
   * beginning of the string use <code>start = 0</code>. Negative start and
   * end positions can be used to specify offsets relative to the end of the
   * String.
   * </p>
   * <p/>
   * <p>
   * If <code>start</code> is not strictly to the left of <code>end</code>, ""
   * is returned.
   * </p>
   * <p/>
   * <pre>
   * substring(null, *, *)    = null
   * substring("", * ,  *)    = "";
   * substring("abc", 0, 2)   = "ab"
   * substring("abc", 2, 0)   = ""
   * substring("abc", 2, 4)   = "c"
   * substring("abc", 4, 6)   = ""
   * substring("abc", 2, 2)   = ""
   * substring("abc", -2, -1) = "b"
   * substring("abc", -4, 2)  = "ab"
   * </pre>
   *
   * @param str   the String to get the substring from, may be null
   * @param start the position to start from, negative means count back from the
   *              end of the String by this many characters
   * @param end   the position to end at (exclusive), negative means count back
   *              from the end of the String by this many characters
   * @return substring from start position to end positon, <code>null</code>
   * if null String input
   */
  public static String substring(String str, int start, int end) {
    if (str == null) {
      return null;
    }

    // handle negatives
    if (end < 0) {
      end = str.length() + end; // remember end is negative
    }
    if (start < 0) {
      start = str.length() + start; // remember start is negative
    }

    // check length next
    if (end > str.length()) {
      end = str.length();
    }

    // if start is greater than end, return ""
    if (start > end) {
      return "";
    }

    if (start < 0) {
      start = 0;
    }
    if (end < 0) {
      end = 0;
    }

    return str.substring(start, end);
  }

  public int setLicense(String licName, String id) throws Exception {
    // license first
    InputStream licInputStream = new BufferedInputStream(
        new FileInputStream(licName));

    // passing file length - as stream.avaialable is not consistent
    int inputLicSize = (int) new File(licName).length();

    licInputStream.mark(inputLicSize);
    // extract the license version and algo number
    byte[] bytesForLicenseVer = new byte[66]; // 64 is sig, then 2 bytes are
    // what I need
    licInputStream.read(bytesForLicenseVer);
    int licenseVer = bytesForLicenseVer[64]; // after 64 - 1st byte
    // if licenseVer is not what I need
    if (licenseVer != 1) {
      licInputStream.close();
      return -1; //
    }
    int algoVer = bytesForLicenseVer[65]; // after 64 - 2nd byte

    bytesForLicenseVer = null;

    // get transfer key to use
    // byte[] digest = getTransferKeyWithDigest("123", "udid", id,
    // "android",
    // algoVer);
    licKey = getTransferKeyWithDigest(ClientInfoHelper.getClientId(),
        DeviceInfoHelper.getDeviceInfo().getDeviceId(), id, "android", algoVer);

    licInputStream.reset(); // to initial mark

    byte[] license = decryptContent(licInputStream, true);

    // now free the mem
    licInputStream.close();
    licKey = null;

    String licenseXml = new String(license);

    key = licenseXml.substring(licenseXml.indexOf("<invalid>")
        + "<invalid>".length(), licenseXml.indexOf("</invalid>"));

    // all fine
    return 0;
  }

  public byte[] decryptContent(InputStream input, boolean isLicense)
      throws Exception {
    // read from inputStream to signature, iv, and data

    // signature is 64 bytes
    byte[] signatureBytes = new byte[64];
    input.read(signatureBytes);

    // splitting the data into parts first so that it can be used without
    // any extra copies in mem later

    // next two bytes to be ignored for license (already used before coming
    // here)
    byte[] licenseInitial2bytes = null;
    if (isLicense) {
      licenseInitial2bytes = new byte[2];
      input.read(licenseInitial2bytes);
    }

    // next 16 bytes for IV Spec
    byte[] ivSpec = new byte[16];
    input.read(ivSpec);

    // rest is data
    byte[] data = FileUtil.readData(input);

    // for signature verification, we need first 16 kb
    // but as the data itself can be smaller, calculate exact amount of
    // bytes to be verified
    int totalBytesStillToBeTaken = 0;
    if (null != licenseInitial2bytes) {
      totalBytesStillToBeTaken += 2; // Initial bytes for license
    }
    totalBytesStillToBeTaken += 16; // IV
    if (data.length > ((16 * 1024) - totalBytesStillToBeTaken)) {
      totalBytesStillToBeTaken = 16 * 1024;
    } else {
      totalBytesStillToBeTaken += data.length;
    }

    // now gather all the bytes
    byte[] dataForSignature = new byte[totalBytesStillToBeTaken];
    int iteratorFordataForSignature = 0;
    if (null != licenseInitial2bytes) {
      System.arraycopy(licenseInitial2bytes, 0, dataForSignature,
          iteratorFordataForSignature, 2);
      iteratorFordataForSignature += 2;
      licenseInitial2bytes = null; // no longer needed
    }
    // next copy the iv data
    System.arraycopy(ivSpec, 0, dataForSignature,
        iteratorFordataForSignature, 16);
    iteratorFordataForSignature += 16;
    // now remaining bytes to make it 16kb
    int remainingBytes = totalBytesStillToBeTaken
        - iteratorFordataForSignature;
    System.arraycopy(data, 0, dataForSignature,
        iteratorFordataForSignature, remainingBytes);

    // get first 16 KB
    if (false == verifySingature(dataForSignature, signatureBytes)) {
      return null; // cannot proceed
    }
    // not needed any more
    dataForSignature = null;
    signatureBytes = null;

    // final decrypting
    if (isLicense) {
      return decryptData(data, ivSpec, licKey);
    } else {
      return decryptData(data, ivSpec, Base64.decode(key, 0));
    }
  }

  private byte[] decryptData(byte[] data, byte[] ivSpec, byte[] key)
      throws InvalidKeyException, IllegalBlockSizeException,
      BadPaddingException, UnsupportedEncodingException,
      NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidAlgorithmParameterException, InvalidParameterSpecException {
    SecretKeySpec secretKey = new SecretKeySpec(key, IOUtilsExtraHelper.SYMMETRIC_ALGORITHM);
    IvParameterSpec spec = new IvParameterSpec(ivSpec);
    Cipher cipher = Cipher.getInstance(IOUtilsExtraHelper.SYMMETRIC_CIPHER);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
    byte[] decryptedData = cipher.doFinal(data);
    cipher = null;
    data = null;
    ivSpec = null;
    return decryptedData;
  }

  // for use within app to store various secrets
  public byte[] decryptDataWithTransferKey(byte[] data, @NonNull String clientId,
                                           @NonNull String uDID)
      throws InvalidKeyException, IllegalBlockSizeException,
      BadPaddingException, UnsupportedEncodingException,
      NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidAlgorithmParameterException, InvalidParameterSpecException {
    SecretKeySpec secretKey = new SecretKeySpec(getTransferKeyWithDigest(clientId, uDID),
        IOUtilsExtraHelper.SYMMETRIC_ALGORITHM);
    IvParameterSpec spec = new IvParameterSpec(copyOfRange(data, 0, 16));
    Cipher cipher = Cipher.getInstance(IOUtilsExtraHelper.SYMMETRIC_CIPHER);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
    byte[] decryptedData = cipher
        .doFinal(copyOfRange(data, 16, data.length));
    cipher = null;
    data = null;
    return decryptedData;
  }

  // Substring
  // -----------------------------------------------------------------------

  // for use within app to store various secrets
  public byte[] decryptDataWithCustomKey(byte[] data)
      throws InvalidKeyException, IllegalBlockSizeException,
      BadPaddingException, UnsupportedEncodingException,
      NoSuchAlgorithmException, NoSuchPaddingException,
      InvalidAlgorithmParameterException, InvalidParameterSpecException {
    SecretKeySpec secretKey = new SecretKeySpec(getTransferKeyWithDigest(
        "786957341212", "89723108972190372903"),
        IOUtilsExtraHelper.SYMMETRIC_ALGORITHM);
    IvParameterSpec spec = new IvParameterSpec(copyOfRange(data, 0, 16));
    Cipher cipher = Cipher.getInstance(IOUtilsExtraHelper.SYMMETRIC_CIPHER);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
    byte[] decryptedData = cipher
        .doFinal(copyOfRange(data, 16, data.length));
    cipher = null;
    data = null;
    return decryptedData;
  }

  private boolean verifySingature(byte[] message, byte[] signatureBytes)
      throws NoSuchAlgorithmException, SignatureException,
      InvalidKeySpecException, InvalidKeyException {
    Signature signature = Signature.getInstance("SHA1withRSA");

    // rsa_1024_public key is a constant String
    String rsa_1024_public_key =
        "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKl9fT+uhlHkO9xZbGOx2gp/91iuDb1q3DsBQ3jj5gA4BaY3iYJBeNjSDCZBlVBy7JLKQXLbpLHlhZPD9N7HZCMCAwEAAQ==";

    byte[] publicKeyBytes = Base64.decode(rsa_1024_public_key, 0);
    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
        new X509EncodedKeySpec(publicKeyBytes));

    signature.initVerify(publicKey);
    // BigInteger Nvalue = publicKey.getPublicExponent();

    signature.update(message);

    boolean signatureVerification = signature.verify(signatureBytes);
    return signatureVerification;
  }

  public byte[] getTransferKeyWithDigest(String clientId, String udid)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    // choosing algo 1 - secretKey, clientId, udid
    return getTransferKeyWithDigest(clientId, udid, Constants.EMPTY_STRING,
        Constants.EMPTY_STRING, 1);
  }

  public byte[] getTransferKeyWithDigest(String clientId, String udid,
                                         String itemId, String client, int algoVer)
      throws UnsupportedEncodingException, NoSuchAlgorithmException {
    StringBuilder transferKey = new StringBuilder();

    String secretKey = "!a#[JdNt&]pU^$KDzgVTF9wpsaGw%Ju^ZQb3&vQC]-L$8xre5=+UECx@f&8*as)2"; // dummy

    switch (algoVer) {

      // Algo 1: secretKey, clientId , udid
      case 1:
        // secret<0-4> + clientId<0-1> + secret<5-8> + udid<0-1> +
        // secret<9-15>
        // + udid<2-5> + clientId<2-5> + secret<16-20> + clientId<6-9> +
        // udid<6-12>
        // + secret<21-33> + udid<remaining> + secret<34-40> +
        // clientId<remaining> + secret<remaining>

        transferKey.append(substring(secretKey, 0, 5))
            .append(substring(clientId, 0, 2))
            .append(substring(secretKey, 5, 9))
            .append(substring(udid, 0, 2))
            .append(substring(secretKey, 9, 16))
            .append(substring(udid, 2, 6))
            .append(substring(clientId, 2, 6))
            .append(substring(secretKey, 16, 21))
            .append(substring(clientId, 6, 10))
            .append(substring(udid, 6, 13))
            .append(substring(secretKey, 21, 34))
            .append(substring(udid, 13))
            .append(substring(secretKey, 34, 41))
            .append(substring(clientId, 10))
            .append(substring(secretKey, 41));
        break;

      // Algo 2: secretKey, clientId , itemId
      case 2:
        transferKey.append(substring(clientId, 0, 5))
            .append(substring(secretKey, 0, 2))
            .append(substring(itemId, 0, 2))
            .append(substring(secretKey, 2, 7))
            .append(substring(itemId, 2, 6))
            .append(substring(secretKey, 7, 17))
            .append(substring(clientId, 5, 9))
            .append(substring(itemId, 6, 13))
            .append(substring(secretKey, 17, 27))
            .append(substring(clientId, 9, 10))
            .append(substring(secretKey, 27, 34))
            .append(substring(itemId, 13))
            .append(substring(secretKey, 34, 41))
            .append(substring(clientId, 10))
            .append(substring(secretKey, 41));
        break;

      // Algo 3: secretKey, clientId , client
      case 3:
        transferKey.append(substring(client, 0, 4))
            .append(substring(clientId, 0, 3))
            .append(substring(secretKey, 0, 8))
            .append(substring(clientId, 3, 7))
            .append(substring(secretKey, 8, 10))
            .append(substring(client, 4, 6))
            .append(substring(secretKey, 10, 15))
            .append(substring(client, 6, 10))
            .append(substring(secretKey, 15, 20))
            .append(substring(clientId, 7, 13))
            .append(substring(secretKey, 20, 31))
            .append(substring(client, 10))
            .append(substring(secretKey, 31))
            .append(substring(clientId, 13));
        break;

      // Algo 4: secretKey, udid, itemId
      case 4:
        transferKey.append(substring(itemId, 0, 1))
            .append(substring(secretKey, 0, 3))
            .append(substring(udid, 0, 6))
            .append(substring(itemId, 1, 3))
            .append(substring(udid, 6, 7))
            .append(substring(secretKey, 3, 10))
            .append(substring(udid, 7, 10))
            .append(substring(secretKey, 10, 17))
            .append(substring(itemId, 6, 10))
            .append(substring(secretKey, 17, 23))
            .append(substring(udid, 10))
            .append(substring(secretKey, 23, 31))
            .append(substring(itemId, 10))
            .append(substring(secretKey, 31));
        break;

      // Algo 5: secretKey, udid, client
      case 5:
        transferKey.append(substring(secretKey, 0, 13))
            .append(substring(client, 0, 5))
            .append(substring(udid, 0, 2))
            .append(substring(secretKey, 13, 15))
            .append(substring(client, 5, 6))
            .append(substring(udid, 2, 5))
            .append(substring(secretKey, 15, 17))
            .append(substring(udid, 5, 14))
            .append(substring(secretKey, 17, 25))
            .append(substring(client, 6, 10))
            .append(substring(secretKey, 25, 29))
            .append(substring(udid, 14)).append(substring(client, 10))
            .append(substring(secretKey, 29));
        break;

      // Algo 6: secretKey, itemId, client
      case 6:
        transferKey.append(substring(secretKey, 0, 4))
            .append(substring(itemId, 0, 7))
            .append(substring(client, 0, 1))
            .append(substring(secretKey, 4, 12))
            .append(substring(itemId, 7, 13))
            .append(substring(client, 1, 3))
            .append(substring(secretKey, 12, 18))
            .append(substring(client, 3, 6))
            .append(substring(secretKey, 18, 28))
            .append(substring(itemId, 13, 17))
            .append(substring(secretKey, 28, 32))
            .append(substring(itemId, 17)).append(substring(client, 6))
            .append(substring(secretKey, 32));
        break;

      // Algo 7: secretKey, clientId, udid, itemId
      case 7:
        transferKey.append(substring(secretKey, 0, 4))
            .append(substring(clientId, 0, 3))
            .append(substring(udid, 0, 7))
            .append(substring(client, 0, 1))
            .append(substring(clientId, 3, 7))
            .append(substring(secretKey, 4, 12))
            .append(substring(udid, 7, 13))
            .append(substring(clientId, 7, 9))
            .append(substring(client, 1, 3))
            .append(substring(secretKey, 12, 18))
            .append(substring(client, 3, 6))
            .append(substring(clientId, 9))
            .append(substring(secretKey, 18, 28))
            .append(substring(udid, 13, 17))
            .append(substring(secretKey, 28, 32))
            .append(substring(udid, 17)).append(substring(client, 6))
            .append(substring(secretKey, 32));
        break;

      // Algo 8: secretKey, clientId, udid, client
      case 8:
        transferKey.append(substring(secretKey, 0, 13))
            .append(substring(client, 0, 5))
            .append(substring(clientId, 0, 5))
            .append(substring(udid, 0, 2))
            .append(substring(secretKey, 13, 15))
            .append(substring(client, 5, 6))
            .append(substring(udid, 2, 5))
            .append(substring(clientId, 5, 7))
            .append(substring(secretKey, 15, 17))
            .append(substring(udid, 5, 14))
            .append(substring(secretKey, 17, 25))
            .append(substring(clientId, 7, 8))
            .append(substring(client, 6, 10))
            .append(substring(secretKey, 25, 29))
            .append(substring(udid, 14)).append(substring(clientId, 8))
            .append(substring(client, 10))
            .append(substring(secretKey, 29));
        break;

      // Algo 9: secretKey, udid, itemId, client
      case 9:
        transferKey.append(substring(client, 0, 2))
            .append(substring(itemId, 0, 1))
            .append(substring(secretKey, 0, 3))
            .append(substring(client, 2, 5))
            .append(substring(itemId, 1, 3))
            .append(substring(udid, 0, 6))
            .append(substring(secretKey, 3, 10))
            .append(substring(udid, 6, 7))
            .append(substring(secretKey, 10, 17))
            .append(substring(udid, 7, 10))
            .append(substring(client, 5))
            .append(substring(itemId, 6, 10))
            .append(substring(secretKey, 17, 23))
            .append(substring(udid, 10))
            .append(substring(secretKey, 23, 31))
            .append(substring(itemId, 10))
            .append(substring(secretKey, 31));
        break;

      // Algo 10: secretKey, clientId, udid, itemId, client
      case 10:
        transferKey.append(substring(clientId, 0, 5))
            .append(substring(client, 0, 5))
            .append(substring(itemId, 0, 3))
            .append(substring(secretKey, 0, 7))
            .append(substring(client, 5))
            .append(substring(clientId, 5, 7))
            .append(substring(itemId, 3, 5))
            .append(substring(udid, 0, 6))
            .append(substring(secretKey, 7, 10))
            .append(substring(udid, 6, 7))
            .append(substring(secretKey, 10, 14))
            .append(substring(udid, 7, 10))
            .append(substring(clientId, 7, 9))
            .append(substring(itemId, 5, 7))
            .append(substring(secretKey, 14, 18))
            .append(substring(udid, 10)).append(substring(clientId, 9))
            .append(substring(secretKey, 18, 26))
            .append(substring(itemId, 7))
            .append(substring(secretKey, 26));
        break;

    }

    String tempKey = transferKey.toString();
    Logger.e("BOOK_READER", tempKey);

    // hasing transferKey
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(transferKey.toString().getBytes());

    return md.digest();
  }
}
