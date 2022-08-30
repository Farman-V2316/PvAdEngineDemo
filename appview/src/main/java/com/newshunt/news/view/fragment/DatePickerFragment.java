/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.widget.DatePicker;

import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.appview.R;
import com.newshunt.news.view.listener.AstroDateSelectedListener;

import java.util.Calendar;

/**
 * Created by anshul on 16/2/17.
 */

public class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

  private AstroDateSelectedListener listener;
  private Calendar calendar;


  public static DatePickerFragment newInstance(AstroDateSelectedListener listener,
                                               Calendar calendar) {
    DatePickerFragment datePickerFragment = new DatePickerFragment();
    datePickerFragment.setListener(listener);
    datePickerFragment.setCalendar(calendar);
    return datePickerFragment;
  }

  private void setListener(AstroDateSelectedListener listener) {
    this.listener = listener;
  }

  private void setCalendar(Calendar calendar) {
    this.calendar = calendar;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Set the default date to 1 January 2000 on the date picker.
    int year = DailyhuntConstants.DEFAULT_ASTRO_YEAR;
    int month = DailyhuntConstants.DEFAULT_ASTRO_MONTH;
    int day = DailyhuntConstants.DEFAULT_ASTRO_DAY;

    if (calendar != null) {
      year = calendar.get(Calendar.YEAR);
      month = calendar.get(Calendar.MONTH);
      day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    //Create a new instance of DatePickerDialog and return it
    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
        R.style.AstroDatePickerDialogTheme, this, year, month, day);
    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    return datePickerDialog;
  }

  public void onDateSet(DatePicker view, int year, int month, int day) {
    if (listener != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.YEAR, year);
      calendar.set(Calendar.MONTH, month);
      calendar.set(Calendar.DAY_OF_MONTH, day);
      listener.onDateSet(calendar);
    }
  }
}
