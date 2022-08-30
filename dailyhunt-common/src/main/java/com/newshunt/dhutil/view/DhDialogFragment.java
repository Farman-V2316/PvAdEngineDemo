/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dhutil.R;
import com.newshunt.dataentity.dhutil.model.entity.asset.DialogDetail;


/**
 * Common DailyHunt Dialog Fragment to be used across the modules
 *
 * @author shashikiran.nr
 */
public class DhDialogFragment extends DialogFragment {

  private static final String STOP_SAVING = "stopSaving";
  private static final String REMOVE_ARTICLES = "removeArticles";
  private static final String DELETE_ARTICLES = "deleteArticles";
  private static final String DELETE_Notifications = "deleteNotifications";
  private DhDialogListener dhDialogListener;
  private String dialogTitleText, dialogHeaderText, dialogPositiveBtnText,
      dialogNegativeBtnText;
  private DialogInterface.OnDismissListener dialogDismissListener;

  private static DhDialogFragment createInstance(DhDialogListener listener,
                                                 String titleText, String headerText,
                                                 String positiveBtnText, String negativeBtnText,
                                                 DialogInterface.OnDismissListener dismissListener) {

    DhDialogFragment dhDialogFragment = new DhDialogFragment();
    dhDialogFragment.dhDialogListener = listener;
    dhDialogFragment.dialogTitleText = titleText;
    dhDialogFragment.dialogHeaderText = headerText;
    dhDialogFragment.dialogPositiveBtnText = positiveBtnText;
    dhDialogFragment.dialogNegativeBtnText = negativeBtnText;
    dhDialogFragment.dialogDismissListener = dismissListener;
    return dhDialogFragment;
  }

  // Utility methods to create objects of this class. Will be shown in news list and details
  public static void showArticleStopSavingDialog(FragmentManager fragmentManager,
                                                 DhDialogListener listener) {
    DhDialogFragment dialog =
        DhDialogFragment.createInstance(listener,
            CommonUtils.getString(com.newshunt.common.util.R.string.stop_saving_article_dialog_title_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.stop_saving_article_dialog_content_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.restore_setting_yes),
            CommonUtils.getString(com.newshunt.common.util.R.string.restore_setting_no), null);
    dialog.show(fragmentManager, STOP_SAVING);
  }

  public static void showRemoveArticleDialog(FragmentManager fragmentManager,
                                             DhDialogListener listener) {

    DhDialogFragment dialog =
        DhDialogFragment.createInstance(listener,
            CommonUtils.getString(com.newshunt.common.util.R.string.remove_saved_articles_dialog_title_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.remove_saved_articles_dialog_content_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.dialog_remove), CommonUtils.getString(com.newshunt.common.util.R.string.dialog_cancel),
            null);
    dialog.show(fragmentManager, REMOVE_ARTICLES);
  }

  public static void showArticleDeleteConfirmationDialog(FragmentManager fragmentManager,
                                                         DhDialogListener listener) {
    DhDialogFragment dialog =
        DhDialogFragment.createInstance(listener,
            CommonUtils.getString(com.newshunt.common.util.R.string.delete_saved_articles_dialog_title_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.remove_saved_articles_dialog_content_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.dialog_delete), CommonUtils.getString(com.newshunt.common.util.R.string.dialog_cancel),
            null);
    dialog.show(fragmentManager, DELETE_ARTICLES);
  }

  public static void showNotificationDeleteConfirmationDialog(FragmentManager fragmentManager,
                                                              DhDialogListener listener) {
    DhDialogFragment dialog =
        DhDialogFragment.createInstance(listener,
            CommonUtils.getString(com.newshunt.common.util.R.string.delete_notification_dialog_title_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.remove_notification_dialog_content_text),
            CommonUtils.getString(com.newshunt.common.util.R.string.dialog_delete), CommonUtils.getString(com.newshunt.common.util.R.string.dialog_cancel),
            null);
    dialog.show(fragmentManager, DELETE_Notifications);
  }

  public static void showDialog(FragmentManager fragmentManager, DialogDetail dialogDetail,
                                DhDialogListener listener) {
    DhDialogFragment dialog =
        DhDialogFragment.createInstance(listener, dialogDetail.getTitle(),
            dialogDetail.getMessage(), dialogDetail.getPositiveButtonText(),
            dialogDetail.getNegativeButtonText(), null);
    dialog.show(fragmentManager, dialogDetail.getTag());
  }

  public static void showDialog(FragmentManager fragmentManager, DialogDetail
      dialogDetail,
                                DhDialogListener listener,
                                DialogInterface.OnDismissListener dismissListener) {
    DhDialogFragment dialog =
        DhDialogFragment.createInstance(listener, dialogDetail.getTitle(),
            dialogDetail.getMessage(), dialogDetail.getPositiveButtonText(),
            dialogDetail.getNegativeButtonText(), dismissListener);
    dialog.show(fragmentManager, dialogDetail.getTag());
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    final Dialog dialog = new Dialog(getActivity());
    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    dialog.setContentView(R.layout.dialogfragment_layout);
    dialog.setCanceledOnTouchOutside(true);

    NHTextView dialogTitle = (NHTextView) dialog.findViewById(R.id.dialogTitletext);
    NHTextView dialogHeader = (NHTextView) dialog.findViewById(R.id.dialogHeaderText);
    NHTextView positiveButton = (NHTextView) dialog.findViewById(R.id.positive_button);
    NHTextView negativeButton = (NHTextView) dialog.findViewById(R.id.negative_button);
    dialogTitle.setText(dialogTitleText);
    dialogHeader.setText(dialogHeaderText);
    positiveButton.setText(dialogPositiveBtnText);

    if (!CommonUtils.isEmpty(dialogNegativeBtnText)) {
      negativeButton.setText(dialogNegativeBtnText);
      negativeButton.setVisibility(View.VISIBLE);
      negativeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          dismiss();
          if (dhDialogListener != null) {
            dhDialogListener.onDialogNegativeClick();
          }
        }
      });
    }

    positiveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
        if (dhDialogListener != null) {
          dhDialogListener.onDialogPositiveClick();
        }
      }
    });

    return dialog;
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (dialogDismissListener != null) {
      dialogDismissListener.onDismiss(dialog);
    }
  }
}
