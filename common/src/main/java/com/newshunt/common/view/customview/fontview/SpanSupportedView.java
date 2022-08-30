/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview.fontview;

import androidx.annotation.NonNull;
import android.text.Spannable;

/**
 * @author: bedprakash on 15/11/17.
 */

public interface SpanSupportedView {

  @NonNull
  Spannable applySpan(@NonNull Spannable string);
}
