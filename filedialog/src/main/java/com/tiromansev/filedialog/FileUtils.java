package com.tiromansev.filedialog;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> getMountPoints(Context context, boolean checkWritable, boolean useOldFileDialog) {
        List<String> mountPoints = new ArrayList<>();
        if (checkWritable) {
            mountPoints.addAll(getWritableMountPoints(context, false, useOldFileDialog));
        }
        else {
            if (!useOldFileDialog && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                for (com.library.mountpoint.MountPoint mountPoint : com.library.mountpoint.MountPoint.getMountPoints(context).values()) {
                    mountPoints.add(new File(mountPoint.getRoot()).getAbsolutePath());
                }
            }
            else {
                mountPoints.addAll(com.library.mountpoint.MountPoint.getPreLollipopMountPoints());
            }
        }
        return mountPoints;
    }

    public static String getAppDir(Context context) {
        return "/Android/data/" + context.getApplicationContext().getPackageName();
    }

    public static List<String> getWritableMountPoints(Context context, boolean addDefault, boolean useOldFileDialog) {
        List<String> mountPoints = new ArrayList<>();
        //default
        if (addDefault) {
            mountPoints.add(context.getResources().getString(R.string.caption_default));
        }
        if (!useOldFileDialog && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            for (com.library.mountpoint.MountPoint mountPoint : com.library.mountpoint.MountPoint.getMountPoints(context).values()) {
                String mountPath = new File(mountPoint.getRoot()).getAbsolutePath();
                if (com.library.mountpoint.MountPoint.checkWriteMountPoint(mountPath + getAppDir(context))) {
                    mountPoints.add(mountPath);
                }
            }
        }
        else {
            mountPoints.addAll(com.library.mountpoint.MountPoint.getPreLollipopMountPoints());
        }
        return mountPoints;
    }

}
