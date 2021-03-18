package com.tiromansev.filedialog.utils;

import android.content.Context;
import android.widget.Toast;

public class GuiUtils {

    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showMessage(Context context, int messageRes) {
        String message = context.getResources().getString(messageRes);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
