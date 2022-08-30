package com.newshunt.common.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Scanner;

/**
 * This is a command line tool to convert or replace a set of xml vectors to selectors. The class
 * appends "vector_" as a prefix to the veector file copied and then create a selector xml with the
 * original file name.This also deletes any png file if present with same name as the
 * original vector file. This take scare oof deletion in all densities
 * folders.
 * <p>
 * Created by bedprakash.rout on 27/9/16.
 */

public class VectorReplaceTool {

  private static final String FILE_SEPARATOR = File.separator;

  public static void main(String args[]) {
    String sourceDir, destinationDir;

    if (args.length == 2) {
      sourceDir = args[0];
      destinationDir = args[1];
    } else {
      Scanner reader = new Scanner(System.in);
      System.out.println("Enter source directory: ");
      sourceDir = reader.nextLine();
      System.out.println("Enter destination directory: ");
      destinationDir = reader.nextLine();
    }

    startTransfer(sourceDir, destinationDir);
  }

  private static void startTransfer(String sourceDirPath, String destinationDirPath) {

    if (sourceDirPath == null || !new File(sourceDirPath).exists()) {
      System.out.println("invalid source ");
      return;
    }

    if (destinationDirPath == null || !new File(destinationDirPath).exists()) {
      System.out.println("invalid destination ");
      return;
    }

    File sourceDir = new File(sourceDirPath);
    if (sourceDir.listFiles().length == 0) {
      System.out.println("0 files  found. Done");
      return;
    } else {
      System.out.println(sourceDir.listFiles().length + " files found.Starting transfer...");
    }

    File drawableFolder = new File(destinationDirPath + FILE_SEPARATOR + "drawable");

    if (!drawableFolder.exists()) {
      System.out.println("Drawable folder not found. Aborting");
      return;
    }

    for (File file : sourceDir.listFiles()) {
      copyFile(file, new File(drawableFolder + FILE_SEPARATOR + "vector_" + file.getName()));
      createDrawable(file, drawableFolder);
      deleteExisting(file, destinationDirPath);
    }

  }


  static void copyFile(File sourceFile, File destFile) {
    try {
      if (!destFile.exists()) {
        destFile.createNewFile();
      }

      FileChannel source = null;
      FileChannel destination = null;
      try {
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        destination.transferFrom(source, 0, source.size());
      } finally {
        if (source != null) {
          source.close();
        }
        if (destination != null) {
          destination.close();
        }
      }
      removeLine(destFile, "android:fillType=\"evenOdd\"");
    } catch (IOException e) {
      System.out.println("error while copying file " + sourceFile.getName());
      e.printStackTrace();
    }
  }


  private static void removeLine(File inputFile, String lineToRemove) {
    try {
      File tempFile = new File("temp_" + inputFile.getName());

      BufferedReader reader = null;

      reader = new BufferedReader(new FileReader(inputFile));

      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

      String currentLine;

      while ((currentLine = reader.readLine()) != null) {
        // trim newline when comparing with lineToRemove
        String trimmedLine = currentLine.trim();
        if (trimmedLine.contains(lineToRemove)) {
          continue;
        }
        writer.write(currentLine + System.getProperty("line.separator"));
      }
      writer.close();
      reader.close();
      if (inputFile.delete()) {
        boolean successful = tempFile.renameTo(inputFile);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void createDrawable(File file, File destinationDir) {
    if (!destinationDir.exists()) {
      System.out.println("invalid destination ");
      return;
    }
    File drawable = new File(destinationDir, file.getName());
    if (drawable.exists()) {
      System.out.println("Warning!! Original file deleted " + file.getName());
      drawable.delete();
    }
    try {
      PrintWriter writer = new PrintWriter(drawable, "UTF-8");
      writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      writer.println("<selector xmlns:android=\"http://schemas.android.com/apk/res/android\">");
      writer.println("    <item android:drawable=\"@drawable/vector_" + getFileName(file) + "\" " +
          "/>");
      writer.println("</selector>");
      writer.close();
    } catch (FileNotFoundException e) {
      System.out.println("invalid destination ");
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

  }


  private static void deleteExisting(File file, String destinationDirPath) {
    deleteInDirectory(file, destinationDirPath + FILE_SEPARATOR + "drawable-hdpi");
    deleteInDirectory(file, destinationDirPath + FILE_SEPARATOR + "drawable-mdpi");
    deleteInDirectory(file, destinationDirPath + FILE_SEPARATOR + "drawable-xhdpi");
    deleteInDirectory(file, destinationDirPath + FILE_SEPARATOR + "drawable-xxhdpi");
    deleteInDirectory(file, destinationDirPath + FILE_SEPARATOR + "drawable-xxxhdpi");


  }

  private static void deleteInDirectory(File file, String destinationDirPath) {
    File destinationFolder = new File(destinationDirPath);
    if (!destinationFolder.exists()) {
      System.out.println("Destination doesn't exist " + destinationFolder.getName());
      return;
    }
    File fileToDelete = new File(destinationFolder, getFileName(file) + ".png");
    if (fileToDelete.exists()) {
      fileToDelete.delete();
    }
  }


  static String getFileName(File file) {
    return file.getName().split("\\.")[0];
  }
}