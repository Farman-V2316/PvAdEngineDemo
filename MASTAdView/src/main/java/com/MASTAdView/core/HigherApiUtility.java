package com.MASTAdView.core;

import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.WebView;

import com.MASTAdView.MASTAdLog;

/**
 * Class to Handle higher api calls
 * 
 * @author Jayananda.sagar
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HigherApiUtility {
	/**
	 * Function which is meant for enabling transparancy for higher
	 */
	public static void forceTransparancy(WebView aWebView) {

		aWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
	}

	/**
	 * return the public folder with Pictures
	 * 
	 * @return
	 */
	public static File getPublicFolderForPictures() {
		File folder = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return folder;
	}

	/**
	 * After changing the file, rescan the fole structure f
	 * 
	 * @param aContext
	 * @param outputFile
	 * @param adLog
	 */
	public static void ScanFileAfterWrite(Context aContext, File outputFile,
			final MASTAdLog adLog) {
		MediaScannerConnection.scanFile(aContext,
				new String[] { outputFile.toString() }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
						adLog.log(MASTAdLog.LOG_LEVEL_DEBUG,
								"JavascriptInterface",
								"storePicture done, media scaner run");
					}
				});
	}

	/**
	 * 
	 * @param aContext
	 */
	public static boolean isScreenOn(Context aContext) {
		boolean isScreenOn = true;
		android.os.PowerManager pm = (android.os.PowerManager) aContext
				.getSystemService(Context.POWER_SERVICE);
		if (null != pm && !pm.isScreenOn()) {
			isScreenOn = false;
		} else {
			isScreenOn = true;
		}
		return isScreenOn;
	}

	/**
	 * Funciton to give Cache Folder, makes use of API Level 8 and above
	 * 
	 * @return
	 */
	public static String getCacheFolder(Context aContext) {
		return aContext.getExternalCacheDir().getAbsolutePath();
	}
}
