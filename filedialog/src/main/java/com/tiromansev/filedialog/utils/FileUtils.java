package com.tiromansev.filedialog.utils;

import android.app.Activity;

import java.text.DecimalFormat;

public class FileUtils {

    public static String getInternalAppDir(Activity context) {
        return context.getFilesDir().getAbsolutePath() + "/";
    }

    public static String size(long size) {
        String hrSize = "";
        double k = size / 1024;
        double m = size / 1048576;
        double g = size / 1073741824;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (k <= 0) {
            hrSize = dec.format(k).concat(" b ");
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
