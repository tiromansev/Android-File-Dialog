package com.tiromansev.filedialog;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Intent;
import android.provider.Settings;

public class PermissionUtils {

    final static String MANAGE_EXTERNAL_STORAGE_PERMISSION = "android:manage_external_storage";

    public static boolean hasManageStoragePermission(Activity activity) {
        if (CommonUtils.isRVersion()) {
            AppOpsManager appOpsManager = activity.getSystemService(AppOpsManager.class);
            if (appOpsManager != null) {
                return appOpsManager.unsafeCheckOpNoThrow(
                        MANAGE_EXTERNAL_STORAGE_PERMISSION,
                        activity.getApplicationInfo().uid,
                        activity.getPackageName()
                ) == AppOpsManager.MODE_ALLOWED;
            }
        }

        return true;
    }

    public static Intent getManageStoragePermissionIntent() {
        return new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
    }

}
