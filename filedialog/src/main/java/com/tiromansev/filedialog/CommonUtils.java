package com.tiromansev.filedialog;

import android.os.Build;

public class CommonUtils {

    public static boolean isRVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

}
