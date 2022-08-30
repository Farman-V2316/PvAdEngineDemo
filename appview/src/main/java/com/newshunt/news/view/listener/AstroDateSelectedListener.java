/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.listener;

import java.util.Calendar;

/**
 * Interface for communicating the date selected by the user in the date picker dialog.
 * Created by anshul on 16/2/17.
 */

public interface AstroDateSelectedListener {
  void onDateSet(Calendar calendar);
}
