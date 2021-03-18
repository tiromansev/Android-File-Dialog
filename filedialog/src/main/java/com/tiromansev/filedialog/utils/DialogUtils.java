package com.tiromansev.filedialog.utils;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;

import com.tiromansev.filedialog.BaseCallback;
import com.tiromansev.filedialog.R;

public class DialogUtils {

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
