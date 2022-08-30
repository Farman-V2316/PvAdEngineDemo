/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.app.helper;

import androidx.core.content.FileProvider;
/**
 * Empty Class, extending FileProvider -  As direct use of FileProvider class in manifest throws exception
 *
 * https://github.com/stkent/bugshaker-android/issues/108
 * https://stackoverflow.com/questions/28095703/manifest-merger-failed-error
 *
 * @author umesh.isran on 05/07/18.
 */
public class AppFileProvider extends FileProvider {
}
