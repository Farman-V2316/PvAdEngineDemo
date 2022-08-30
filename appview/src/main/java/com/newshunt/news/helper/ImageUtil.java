package com.newshunt.news.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.bumptech.glide.request.transition.Transition;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.ImageSaveFailureReason;
import com.newshunt.common.helper.listener.ImageSaveCallBack;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.sdk.network.image.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Utility class for Image saving through picasso utils
 *
 * @author shreyasdesai on 22/8/15.
 */
public class ImageUtil {
  private static final String DAILY_HUNT_IMAGE_FOLDER = "Dailyhunt";

  private static final String MEDIA_MIME = "image/png/jpg";
  private static final String PICTURES_FOLDER = "Pictures/";

  /**
   * create folder and save image using Target class of picasso library.
   *
   * @param context       - context
   * @param fullScreenUrl - imageurl path to download from and save
   * @param storyId
   * @return - returns ImageTarget
   */
  @NonNull
  public static Image.ImageTarget getSaveImageTarget(final Context context, final String
      fullScreenUrl, final String storyId, final ImageSaveCallBack saveCallBack) {
    return new Image.ImageTarget() {


      @Override
      public void onResourceReady(@NonNull Object bitmap, @Nullable Transition transition) {

        if (!(bitmap instanceof Bitmap)) {
          return;
        }

        CommonUtils.runInBackground(() -> {
          if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {

            String path = Environment.getExternalStorageDirectory() + Constants.FORWARD_SLASH +
                DAILY_HUNT_IMAGE_FOLDER + Constants.FORWARD_SLASH;
            File folder = new File(path);
            if (!folder.exists()) {
              folder.mkdir();
            }
            try {
              final String imagePath =
                  folder + File.separator + storyId + System.currentTimeMillis() + Uri.parse(fullScreenUrl).getLastPathSegment();

              final String fileSavedPath = imagePath.endsWith(Constants.Extensions.DOT_WEBP) ?
                  imagePath.replaceAll(Constants.Extensions.DOT_WEBP, Constants.Extensions.DOT_JPG) :
                  imagePath;

              saveImageToStream(context, (Bitmap) bitmap, fileSavedPath, new FileOutputStream(fileSavedPath), saveCallBack);
            } catch (Exception e) {
              if (saveCallBack != null) {
                saveCallBack.onImageSaveFailure(ImageSaveFailureReason.NETWORK);
              }
            }
          } else {
            final String imagePath = storyId + System.currentTimeMillis() + Uri.parse(fullScreenUrl).getLastPathSegment();
            final String fileSavedPath = imagePath.endsWith(Constants.Extensions.DOT_WEBP) ?
                imagePath.replaceAll(Constants.Extensions.DOT_WEBP, Constants.Extensions.DOT_JPG) : imagePath;

            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileSavedPath);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, MEDIA_MIME);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, PICTURES_FOLDER + DAILY_HUNT_IMAGE_FOLDER);
            //Inserts a row into a table at the given URL.
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            try {
              if (imageUri != null) {
                saveImageToStream(context, (Bitmap) bitmap, imageUri.getPath(), resolver.openOutputStream(imageUri), saveCallBack);
              }
            } catch (FileNotFoundException e) {
              if (saveCallBack != null) {
                saveCallBack.onImageSaveFailure(ImageSaveFailureReason.NETWORK);
              }
            }
          }
        });
      }
      @Override
      public void onLoadFailed(@Nullable Drawable errorDrawable) {
        super.onLoadFailed(errorDrawable);
        if (saveCallBack != null) {
          saveCallBack.onImageSaveFailure(ImageSaveFailureReason.NETWORK);
        }
      }
    };
  }

  private static void saveImageToStream(Context context, Bitmap bitmap, String fileSavedPath,
                                        OutputStream openOutputStream, final ImageSaveCallBack saveCallBack) {
    try {
      bitmap.compress(Bitmap.CompressFormat.JPEG, 80, openOutputStream);
      openOutputStream.flush();
      openOutputStream.close();
      scanFile(context, fileSavedPath, null);
      if (saveCallBack != null) {
        saveCallBack.onImageSaveSuccess();
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (saveCallBack != null) {
        saveCallBack.onImageSaveFailure(ImageSaveFailureReason.FILE);
      }
    }
  }

  public static void scanFile(Context context, String path, String mimeType) {
    CustomPhotoScan customPhotoScan = new CustomPhotoScan(path, mimeType);
    MediaScannerConnection connection =
        new MediaScannerConnection(context, customPhotoScan);
    customPhotoScan.connection = connection;
    connection.connect();
  }


  /**
   * this class used to scan and display save image in gallery.
   */
  private static final class CustomPhotoScan
      implements MediaScannerConnection.MediaScannerConnectionClient {
    private final String path;
    private final String mimeType;
    MediaScannerConnection connection;

    public CustomPhotoScan(String path, String mimeType) {
      this.path = path;
      this.mimeType = mimeType;
    }

    @Override
    public void onMediaScannerConnected() {
      connection.scanFile(path, mimeType);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
      connection.disconnect();
    }
  }

}
