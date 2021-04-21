package com.tiromansev.filedialog.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tiromansev.filedialog.R;

public class GuiUtils {

    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showMessage(Context context, int messageRes) {
        String message = context.getResources().getString(messageRes);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void tryToStartIntentFoResult(Activity context, Intent intent, int requestCode) {
        try {
            context.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            GuiUtils.showMessage(context, R.string.message_intent_not_have_activity);
        }
    }

}
