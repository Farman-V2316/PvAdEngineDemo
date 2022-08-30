/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.appview.R;
import com.newshunt.sdk.network.connection.ConnectionSpeed;
import androidx.fragment.app.DialogFragment;

/**
 * @author shashikiran.nr
 */
public class SavedArticleCoachMarkDialogFragment extends DialogFragment {

  public Adapter mListener;

  public static SavedArticleCoachMarkDialogFragment createInstance(
      Adapter listener) {
    SavedArticleCoachMarkDialogFragment coachMarkDialogFragment = new
        SavedArticleCoachMarkDialogFragment();
    coachMarkDialogFragment.setListener(listener);
    return coachMarkDialogFragment;
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (mListener != null) {
      mListener.onDismiss();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    observeConnectivityChanges();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Context context = getActivity();
    int textColor = context.getResources().getColor(R.color.saved_article_coachmark_text_color);
    String description = getResources().getString(R.string
        .saved_article_coachmark_text, textColor);
    Dialog mDialog = createStickyDialog(context);

    setDialogHeightBasedOnLanguage(context, mDialog);
    TextView stickyDescription = (TextView) mDialog.findViewById(R.id.sticky_desc);
    stickyDescription.setText(Html.fromHtml(description));

    Window window = mDialog.getWindow();
    WindowManager.LayoutParams layoutParams = window.getAttributes();
    layoutParams.gravity = Gravity.BOTTOM;
    layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
    window.setAttributes(layoutParams);

    setDialogWidth(mDialog);


    View stickyView = mDialog.findViewById(R.id.sticky_desc);
    stickyView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
        if (mListener != null) {
          mListener.onCoachClick();
        }
      }
    });

    ImageView cancelIcon = (ImageView) mDialog.findViewById(R.id.cancel_icon);
    cancelIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
        if (mListener != null) {
          mListener.onCancelClick();
        }

      }
    });

    return mDialog;
  }

  private Dialog createStickyDialog(Context context) {
    Dialog dialog = new Dialog(context, R.style.DialogSlideAnim);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    dialog.setContentView(R.layout.sticky_coachmark);
    dialog.setCanceledOnTouchOutside(true);
    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    return dialog;
  }

  private void setDialogHeightBasedOnLanguage(Context context, Dialog dialog) {
    RelativeLayout parentStickyLayout = (RelativeLayout) dialog.findViewById(R.id.sticky_parent);
    String preferredLanguage = UserPreferenceUtil.getUserPrimaryLanguage();
    if (preferredLanguage.equalsIgnoreCase(Constants.MALYALAM_LANGUAGE_CODE)
        || preferredLanguage.equalsIgnoreCase(Constants.TAMIL_LANGUAGE_CODE)) {
      parentStickyLayout.getLayoutParams().height =
          (int) context.getResources().getDimension(R.dimen.sticky_dialog_height1);//125dp
    } else {
      parentStickyLayout.getLayoutParams().height =
          (int) context.getResources().getDimension(R.dimen.sticky_coachmark_height);//90dp
    }
  }

  private void setDialogWidth(Dialog dialog) {
    int width = CommonUtils.getDeviceScreenWidth();
    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
    layoutParams.copyFrom(dialog.getWindow().getAttributes());
    layoutParams.width = width;
    dialog.getWindow().setAttributes(layoutParams);
  }

  private void observeConnectivityChanges() {
    AndroidUtils.connectionSpeedLiveData.observe(SavedArticleCoachMarkDialogFragment.this,
        connectionSpeedEvent -> {
          try {
            if (connectionSpeedEvent.getConnectionSpeed() != ConnectionSpeed.NO_CONNECTION) {
              dismiss();
            }
          } catch (Exception ex) {
            Logger.caughtException(ex);
          }
        });
  }

  public void setListener(Adapter mListener) {
    this.mListener = mListener;
  }

  public interface Adapter {
    void onCoachClick();
    void onCancelClick();
    void onDismiss();
  }
}
