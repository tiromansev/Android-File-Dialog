package com.tiromansev.filedialog;

import android.content.Context;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> getMountPoints(Context context, boolean checkWritable, boolean useOldFileDialog) {
        List<String> mountPoints = new ArrayList<>();
        if (checkWritable) {
            mountPoints.addAll(getWritableMountPoints(context, false, useOldFileDialog));
        } else {
            if (!useOldFileDialog && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                for (MountPoint mountPoint : MountPoint.getMountPoints(context).values()) {
                    mountPoints.add(new File(mountPoint.getRoot()).getAbsolutePath());
                }
            } else {
                mountPoints.addAll(MountPoint.getPreLollipopMountPoints());
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
            for (MountPoint mountPoint : MountPoint.getMountPoints(context).values()) {
                String mountPath = new File(mountPoint.getRoot()).getAbsolutePath();
                if (MountPoint.checkWriteMountPoint(mountPath + getAppDir(context))) {
                    mountPoints.add(mountPath);
                }
            }
        } else {
            mountPoints.addAll(MountPoint.getPreLollipopMountPoints());
        }
        return mountPoints;
    }

    public static String size(long size) {
        String hrSize = "";
        double k = size / 1024;
        double m = size / 1048576;
        double g = size / 1073741824;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (k<= 0) {
            hrSize = String.valueOf(size).concat(" ");
        }
        if (k > 0) {
            hrSize = dec.format(k).concat(" k ");
        }
        if (m > 0) {
            hrSize = dec.format(m).concat(" M ");
        }
        if (g > 0) {
            hrSize = dec.format(g).concat(" G ");
        }

        return hrSize;
    }

}
