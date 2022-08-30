/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.info;

import android.os.Environment;
import android.util.Pair;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Help save and retrieve credentials from external file.
 *
 * @author shreyas.desai
 */
public class CredentialsHelper {
  private static final String DIRECTORY = Environment.getExternalStorageDirectory().toString();
  private static final String APP_CREDENTIALS_BCK_UP = "/.appCredentialsbck";
  private static final String PATH = DIRECTORY + APP_CREDENTIALS_BCK_UP;

  public static void saveClientIdOnFile(String clientId) {
    /* Need to maintain the order. As client is saved first and udId later.
       While saving Client Id, need to retrieve udId and save it again before saving udId. */
    Pair<String, String> info = getCredentialsFromFile();
    String udId = Constants.EMPTY_STRING;
    if (info != null && info.second != null) {
      udId = info.second;
    }
    performFileWriteOperation(clientId, udId);
  }

  public static void saveUdIdOnFile(String udId) {
    /* Need to maintain the order. As client is saved first and udId later.
       While saving UdId, need to retrieve udId and save it again before saving udId. */
    Pair<String, String> info = getCredentialsFromFile();
    String clientId = Constants.EMPTY_STRING;
    if (info != null && info.first != null) {
      clientId = info.first;
    }
    performFileWriteOperation(clientId, udId);
  }

  public static Pair<String, String> getCredentialsFromFile() {
    File backupFile = new File(PATH);
    try {
      FileInputStream inputStream = new FileInputStream(backupFile);
      DataInputStream dataInputStream = new DataInputStream(inputStream);
      String clientId = dataInputStream.readUTF();
      String udId = Constants.EMPTY_STRING;
      dataInputStream.close();
      return Pair.create(clientId, udId);
    } catch (IOException e) {
      Logger.d("FILE_SAVE", "Unable to read from file: " + backupFile);
      return null;
    }
  }

  /**
   * Method to Write the Values to the Credentials File
   *
   * @param value1 -- Value 1 [Client id]
   * @param value2 -- Value 2 [Udid]
   */
  private static void performFileWriteOperation(String value1, String value2) {
    File backupFile = new File(PATH);
    try {
      FileOutputStream outputStream = new FileOutputStream(backupFile);
      DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      dataOutputStream.writeUTF(value1);
      dataOutputStream.writeUTF(value2);
      dataOutputStream.flush();
      dataOutputStream.close();
    } catch (IOException e) {
      Logger.d("FILE_SAVE", "Unable to save the file: " + backupFile);
    }
  }
}
