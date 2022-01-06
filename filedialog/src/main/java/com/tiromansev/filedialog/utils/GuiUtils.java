package com.tiromansev.filedialog.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.tiromansev.filedialog.R;

public class GuiUtils {

    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showMessage(Context context, int messageRes) {
        String message = context.getResources().getString(messageRes);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static <I> boolean tryToStartLauncher(Context context, ActivityResultLauncher<I> launcher, I input) {
        try {
            launcher.launch(input);
            return true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            GuiUtils.showMessage(context, R.string.message_intent_not_have_activity);
            return false;
        }
    }

}
