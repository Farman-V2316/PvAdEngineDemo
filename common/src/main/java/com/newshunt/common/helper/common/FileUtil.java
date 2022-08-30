/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.newshunt.dhutil.helper.preference.AdsPreference.AD_ZIPPED_HTML_CACHE_COUNT;

/**
 * Utility methods for file processing
 *
 * @author arun.babu
 */
public class FileUtil {

  /**
   * The Unix separator character.
   */
  private static final char UNIX_SEPARATOR = '/';

  /**
   * The Windows separator character.
   */
  private static final char WINDOWS_SEPARATOR = '\\';
  private static final String TAG = "FileUtil";

  /**
   * mkdirs will creates a directory on the perticular path
   *
   * @param outdir
   * @param path
   */
  public static void mkdirs(File outdir, String path) {
    File directory = new File(outdir, path);
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  /**
   *
   * @param file
   * @return returns true for file missing.
   */
  public static boolean deleteFolder(File file) {
    boolean result = false;
    try {
      if (file.isDirectory()) {

        // directory is empty, then delete it
        if (file.list().length == 0) {
          result = file.delete();

        } else {

          // list all the directory contents
          String files[] = file.list();

          for (String temp : files) {
            // construct the file structure
            File fileDelete = new File(file, temp);

            // recursive delete
            deleteFolder(fileDelete);
          }

          // check the directory again, if empty then delete it
          if (file.list().length == 0) {
            result = file.delete();
          }
        }

      } else {
        // if file, then delete it
        result = file.delete();
      }
    } catch (Exception e) {
      // e.printStackTrace();
      result = false;
    }
    return result;
  }

  // read data from input stream to byte array
  public static byte[] readData(InputStream in) throws Exception {
    return readData(in, false);
  }

  // gets the directory path given a full file path
  public static String dirpart(String name) {
    int s = name.lastIndexOf(File.separatorChar);
    return s == -1 ? null : name.substring(0, s); // if no separator, then
    // this is not a dir
  }

  public static byte[] readData(InputStream in, boolean removeBOMUtf8)
      throws Exception {
    // IndiCanvas.checkMemory(150 * 1024);

    // start off with 10k byte allocation
    byte[] result = new byte[10240];

    // variable which tells how many byte were completed
    int completed = 0;

    // variable which tells how many bytes was read from stream
    int temp = 0;

    if (removeBOMUtf8) {
      byte[] toCheck = new byte[3];
      in.read(toCheck);
      // detect byte order mark (BOM) for utf-8 if present and if yes,
      // then remove it
      if (toCheck[0] == (byte) 0xef && toCheck[1] == (byte) 0xbb
          && toCheck[2] == (byte) 0xbf) {
        // do nothing
      } else {
        System.arraycopy(toCheck, 0, result, completed, 3);
        completed += 3;
      }
      toCheck = null;
    }

    // First buffer allocated with 1k bytes to read
    byte b[] = new byte[1024];

    byte[] tempArr = null;
    try {
      do {
        // read in chunks of 1024 byte
        temp = in.read(b, 0, 1024);
        if (temp != -1) {
          if (completed + temp > result.length) {
            // successesively increase the result array '
            tempArr = result;
            // result = new byte[completed + temp];
            result = new byte[tempArr.length * 2];
            System.arraycopy(tempArr, 0, result, 0, tempArr.length);
            tempArr = null;
            // System.gc();
          }
          System.arraycopy(b, 0, result, completed, temp);
          completed += temp;
        }

      } while (temp != -1);
    } catch (Exception e) {
      throw e;
    }
    b = null;
    byte[] finalResult = new byte[completed];
    System.arraycopy(result, 0, finalResult, 0, completed);
    result = null;
    return finalResult;
  }

  /**
   * Funciton to give Cache Folder, makes use of API Level 8 and above
   *
   * @return
   */
  public static String getCacheFolder(Context aContext) {
    try {
      return aContext.getExternalCacheDir().getAbsolutePath();
    } catch (Exception e) {
      // if unable to get cacheFolder, return empty
      // crash Fix
      return Constants.EMPTY_STRING;
    }
  }

  public static void saveSerializedObjectInFile(String path, String fileName, Object object) {
    try {
      FileOutputStream fileOut = new FileOutputStream(path + File.separator + fileName, false);
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(object);
      out.close();
      fileOut.close();
    } catch (IOException i) {
      i.printStackTrace();
    }
  }


  public static String writeBitmapToFile(String path, String fileName, Bitmap bitmap) {
    File outputFolder = new File(path);
    if (!outputFolder.exists()) {
      outputFolder.mkdirs();
    }
    File extractedFile = new File(path, fileName);
    BufferedOutputStream outputStream = null;
    if (!extractedFile.exists()) {
      try {
        extractedFile.createNewFile();
        outputStream = new BufferedOutputStream(new FileOutputStream(
            extractedFile.getAbsolutePath()));
        bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);
        closeOutputStream(outputStream);
        return extractedFile.getAbsolutePath();
      } catch (IOException e) {
        Logger.e(TAG, e.getMessage() + " ", e);
      } finally {
        closeOutputStream(outputStream);
      }
    } else {
      // not an error. do not overwrite. can happen if fetched multiple times.
      return extractedFile.getAbsolutePath();
    }
    return null;
  }

  private static void closeOutputStream(OutputStream outputStream) {
    if (outputStream == null) {
      return;
    }
    try {
      outputStream.close();
    } catch (IOException e) {
      Logger.e(TAG, e.getMessage() + " ", e);
    }
  }

  public static String writeToFile(String path, String fileName, String data) {
    File outputFolder = new File(path);
    if (!outputFolder.exists()) {
      outputFolder.mkdirs();
    }
    path += File.separator + fileName;
    BufferedOutputStream outputStream = null;
    try {
      File file = new File(path);
      if (!file.exists()) {
        file.createNewFile();
      }
      outputStream = new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath()));
      outputStream.write(data.getBytes());
      closeOutputStream(outputStream);
      return path;
    } catch (IOException e) {
      Logger.e(TAG, e.getMessage() + " ", e);
    } finally {
      closeOutputStream(outputStream);
    }
    return null;
  }

  public static Object getDeserializedObjectFromFile(String path, String fileName) {
    Object object = null;
    try {
      FileInputStream fileIn = new FileInputStream(path + File.separator + fileName);
      ObjectInputStream in = new ObjectInputStream(fileIn);
      object = in.readObject();
      in.close();
      fileIn.close();
    } catch (IOException i) {
      i.printStackTrace();
      return object;
    } catch (ClassNotFoundException c) {
      c.printStackTrace();
    }
    return object;
  }

  public static void deleteLeastRecentlyAccessed(String basePath) {
    File dir = new File(basePath);
    File[] files = dir.listFiles();
    if (files == null || files.length < PreferenceManager.getPreference(AD_ZIPPED_HTML_CACHE_COUNT, Constants.MAX_NUMBER_OF_FILES)) {
      return;
    }

    File leastRecentlyAccessed = files[0];
    for (int i = 1; i < files.length; i++) {
      if (leastRecentlyAccessed.lastModified() > files[i].lastModified()) {
        leastRecentlyAccessed = files[i];
      }
    }
    if (leastRecentlyAccessed.isDirectory()) {
      for (File child : leastRecentlyAccessed.listFiles()) {
        child.delete();
      }
    }

    leastRecentlyAccessed.delete();
  }


  public static String getLocalFolderForUrl(String basePath, String gifUrl) {
    return basePath + getFileName(gifUrl);
  }

  public static String getFilePath(String basePath, String gifUrl) {
    return getLocalFolderForUrl(basePath, gifUrl) + Constants.FORWARD_SLASH + getFileName
        (gifUrl) + Constants.GIF_EXTENSION;
  }

  public static String getFileName(String gifUrl) {
    return String.valueOf(gifUrl.hashCode());
  }

  /**
   * Method to get file extention.
   *
   * @param fileName File name.
   * @return File extention.
   */
  public static String getExtension(String fileName) {
    String extension = Constants.EMPTY_STRING;
    if (CommonUtils.isEmpty(fileName)) {
      return extension;
    }
    int indexOfLastDot = fileName.lastIndexOf(Constants.DOT);
    if (indexOfLastDot != -1 && indexOfLastDot < fileName.length()) {
      extension = fileName.substring(indexOfLastDot + 1);
    }
    return extension;
  }

  public static Bitmap.CompressFormat getCompressFormatFromFileName(String fileName) {
    switch (getExtension(fileName)) {
      case Constants.Extensions.WEBP:
        return Bitmap.CompressFormat.WEBP;
      case Constants.Extensions.JPEG:
        return Bitmap.CompressFormat.JPEG;
      default:
        return Bitmap.CompressFormat.PNG;
    }
  }

  /**
   * Method to get list of files.
   *
   * @param parentDir  Parent dir.
   * @param extensions Extensions to filter.
   * @return List of selected files.
   */
  public static List<File> getListFiles(File parentDir, String[] extensions) {
    ArrayList<File> inFiles = new ArrayList<File>();
    File[] files = parentDir.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        inFiles.addAll(getListFiles(file, extensions));
      } else {
        for (String extension : extensions) {
          if (file.getName().endsWith(extension)) {
            inFiles.add(file);
          }
        }
      }
    }
    return inFiles;
  }

  public static String getPath(String fileName) {
    if (fileName == null) {
      return null;
    }
    int prefix = getPrefixLength(fileName);
    if (prefix < 0) {
      return null;
    }
    int index = indexOfLastSeparator(fileName);
    int endIndex = index + 1;
    if (prefix >= fileName.length() || index < 0 || prefix >= endIndex) {
      return "";
    }
    return fileName.substring(prefix, endIndex);
  }

  //-----------------------------------------------------------------------

  /**
   * Returns the length of the filename prefix, such as <code>C:/</code> or <code>~/</code>.
   * <p>
   * This method will handle a file in either Unix or Windows format.
   * <p>
   * The prefix length includes the first slash in the full filename
   * if applicable. Thus, it is possible that the length returned is greater
   * than the length of the input string.
   * <pre>
   * Windows:
   * a\b\c.txt           --> ""          --> relative
   * \a\b\c.txt          --> "\"         --> current drive absolute
   * C:a\b\c.txt         --> "C:"        --> drive relative
   * C:\a\b\c.txt        --> "C:\"       --> absolute
   * \\server\a\b\c.txt  --> "\\server\" --> UNC
   *
   * Unix:
   * a/b/c.txt           --> ""          --> relative
   * /a/b/c.txt          --> "/"         --> absolute
   * ~/a/b/c.txt         --> "~/"        --> current user
   * ~                   --> "~/"        --> current user (slash added)
   * ~user/a/b/c.txt     --> "~user/"    --> named user
   * ~user               --> "~user/"    --> named user (slash added)
   * </pre>
   * <p>
   * The output will be the same irrespective of the machine that the code is running on.
   * ie. both Unix and Windows prefixes are matched regardless.
   *
   * @param filename the filename to find the prefix in, null returns -1
   * @return the length of the prefix, -1 if invalid or null
   */
  private static int getPrefixLength(String filename) {
    if (filename == null) {
      return -1;
    }
    int len = filename.length();
    if (len == 0) {
      return 0;
    }
    char ch0 = filename.charAt(0);
    if (ch0 == ':') {
      return -1;
    }
    if (len == 1) {
      if (ch0 == '~') {
        return 2;  // return a length greater than the input
      }
      return isSeparator(ch0) ? 1 : 0;
    } else {
      if (ch0 == '~') {
        int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
        int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
        if (posUnix == -1 && posWin == -1) {
          return len + 1;  // return a length greater than the input
        }
        posUnix = posUnix == -1 ? posWin : posUnix;
        posWin = posWin == -1 ? posUnix : posWin;
        return Math.min(posUnix, posWin) + 1;
      }
      char ch1 = filename.charAt(1);
      if (ch1 == ':') {
        ch0 = Character.toUpperCase(ch0);
        if (ch0 >= 'A' && ch0 <= 'Z') {
          if (len == 2 || !isSeparator(filename.charAt(2))) {
            return 2;
          }
          return 3;
        }
        return -1;

      } else if (isSeparator(ch0) && isSeparator(ch1)) {
        int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
        int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
        if (posUnix == -1 && posWin == -1 || posUnix == 2 || posWin == 2) {
          return -1;
        }
        posUnix = posUnix == -1 ? posWin : posUnix;
        posWin = posWin == -1 ? posUnix : posWin;
        return Math.min(posUnix, posWin) + 1;
      } else {
        return isSeparator(ch0) ? 1 : 0;
      }
    }
  }

  /**
   * Returns the index of the last directory separator character.
   * <p>
   * This method will handle a file in either Unix or Windows format.
   * The position of the last forward or backslash is returned.
   * <p>
   * The output will be the same irrespective of the machine that the code is running on.
   *
   * @param filename the filename to find the last path separator in, null returns -1
   * @return the index of the last separator character, or -1 if there
   * is no such character
   */
  private static int indexOfLastSeparator(String filename) {
    if (filename == null) {
      return -1;
    }
    int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
    int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
    return Math.max(lastUnixPos, lastWindowsPos);
  }

  //-----------------------------------------------------------------------

  /**
   * Checks if the character is a separator.
   *
   * @param ch the character to check
   * @return true if it is a separator character
   */
  private static boolean isSeparator(char ch) {
    return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR;
  }


  /**
   * checks for both file being present and file size non zero
   *
   * @param filePath - path of the file.
   * @return
   */
  public static boolean checkIfFileExists(String filePath) {
    if(CommonUtils.isEmpty(filePath)){
      return false;
    }
    File file = new File(filePath);
    return file.exists() && file.length() != 0;
  }

  public static String getFileNameFromUrl(String url) {
    if (CommonUtils.isEmpty(url)) {
      return Constants.EMPTY_STRING;
    }
    String filteredQueryUrl=UrlUtil.getBaseUrl(url);
    int lastIndex = filteredQueryUrl.lastIndexOf(Constants.FORWARD_SLASH);
    return lastIndex < 0 ? Constants.EMPTY_STRING : filteredQueryUrl.substring(lastIndex,
        filteredQueryUrl.length());
  }

  public static String getHashCodeBasedFileName(String url) {
    return url.hashCode() + Constants.DOT + getExtension(getFileNameFromUrl(url));
  }

  public static String saveBitmapImage(Bitmap bitmap, String path, String url) {
    File folder = new File(path);
    if (!path.endsWith(Constants.FORWARD_SLASH)) {
      path = path + Constants.FORWARD_SLASH;
    }
    if (!folder.exists()) {
      if (!folder.mkdir()) {
        return null;
      }
    }
    try {
      String fileName = FileUtil.getHashCodeBasedFileName(url);
      String imagePath = path + fileName;
      FileOutputStream fileOutputStream = new FileOutputStream(imagePath);
      bitmap.compress(FileUtil.getCompressFormatFromFileName(fileName), 100, fileOutputStream);
      fileOutputStream.close();
      return imagePath;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Uri getFileUri(Context context, String finalFilePath) {
    if(context == null || CommonUtils.isEmpty(finalFilePath)) {
      return null;
    }

    try {
//      Uri.fromFile(new File(finalFilePath)); crashes in target sdk 24 & above
//      https://medium.com/@ali.muzaffar/what-is-android-os-fileuriexposedexception-and-what-you-can-do-about-it-70b9eb17c6d0
      return FileProvider.getUriForFile(
          context, context.getPackageName() + ".fileprovider", new File(finalFilePath));
    } catch (Exception e) {
      Logger.caughtException(e);
      return null;
    }
  }

  //Overloading getFileUri(Context,String)
  public static Uri getFileUri(Context context, File file) {
    if (context == null || file == null) {
      return null;
    }

    try {
      return FileProvider.getUriForFile(
          context, context.getPackageName() + ".fileprovider", file);
    } catch (Exception e) {
      Logger.caughtException(e);
      return null;
    }
  }

  /**
   * Helper method to read a string from a file
   * @param filePath
   * @return String read from the file
   * @throws Exception
   */
  public static String readStringFromFile(final @NonNull String filePath) throws Exception {
    FileInputStream inputStream = null;
    BufferedReader reader = null;
    StringBuilder stringBuilder = new StringBuilder();
    try {
      File file = new File(filePath);
      inputStream = new FileInputStream(file);

      reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append("\n");
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (inputStream != null) {
        inputStream.close();
      }
    }
    return stringBuilder.toString();
  }
}