package com.tiromansev.filedialog.utils;

import android.app.Activity;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.tiromansev.filedialog.BaseCallback;
import com.tiromansev.filedialog.R;

public class DialogUtils {

    public static void showQuestionDialog(Activity context,
                                          String message,
                                          DialogInterface.OnClickListener okListener,
                                          DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder dialog =
                new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        dialog.setTitle(null);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.caption_use_dir, okListener);
        dialog.setNegativeButton(R.string.caption_select_other, cancelListener);
        if (!context.isFinishing()) {
            dialog.show();
        }
    }

    public static void showSimpleDialog(Activity ctx,
                                        String message,
                                        BaseCallback okListener) {
        showSimpleDialog(ctx, message, false, okListener);
    }

    public static void showSimpleDialog(Activity ctx,
                                        String message,
                                        boolean cancelable,
                                        BaseCallback okListener) {
        new AlertDialog.Builder(ctx, R.style.AppCompatAlertDialogStyle)
                .setMessage(message)
                .setTitle(R.string.title_warning)
                .setCancelable(cancelable)
                .setPositiveButton(
                        ctx.getString(R.string.caption_ok), (dialog, which) -> okListener.callBackMethod()).show();
    }

}
