package com.newshunt.common.view.customview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.font.FontHelper;

/**
 * This makes sure the EditText has Focus when it's touched.
 *
 * @author: bedprakash on 3/11/16.
 */

public class CursorAwareEditText extends androidx.appcompat.widget.AppCompatEditText implements View.OnTouchListener{
  public CursorAwareEditText(Context context) {
    super(context);
    init();
  }

  public CursorAwareEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CursorAwareEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init(){
    //set on touch listener
    setOnTouchListener(this);
    //add listener to remove error icon on text changed
    addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        hideError();
      }
    });
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    setFocusableInTouchMode(true);
    return false;
  }

  // to set only default drawable error icon and not show error text
  @Override
  public void setError(CharSequence error, Drawable icon) {
    setCompoundDrawables(null, null, icon, null);
    //show toast with custom textview if error not null
    if (!CommonUtils.isEmpty(error.toString())) {
      FontHelper.showCustomFontToast(getContext(), error.toString(),
        Toast.LENGTH_SHORT, Gravity.CENTER_VERTICAL);
    }
  }

  public void hideError() {
    //remove error icon on text changed
    this.setError(Constants.EMPTY_STRING, null);
  }
}
