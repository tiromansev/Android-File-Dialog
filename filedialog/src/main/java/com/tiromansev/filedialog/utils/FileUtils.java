package com.tiromansev.filedialog.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.tiromansev.filedialog.MountPoint;
import com.tiromansev.filedialog.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String PRIMARY_VOLUME_NAME = "primary";

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

    public static String getInternalAppDir(Activity context) {
        return context.getFilesDir().getAbsolutePath() + "/";
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

        if (k <= 0) {
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

    public static String getFilePathFromUri(Activity context, Uri uri) {
        try {
            if (uri != null) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
                String filePath = "";
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                } else {
                    filePath = uri.getPath();
                }

                if (TextUtils.isEmpty(filePath)) {
                    filePath = getPathFromInputStreamUri(context, uri);
                }

                return filePath;
            }
        } catch (Exception e) {
            e.printStackTrace();
            GuiUtils.showMessage(context, e.getLocalizedMessage());
            return "";
        }
        return "";
    }

    public static String getPathFromInputStreamUri(Activity context, Uri uri) {
        InputStream inputStream = null;
        String filePath = null;

        if (uri.getAuthority() != null) {
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                File photoFile = createTemporalFileFrom(context, inputStream);

                filePath = photoFile.getPath();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return filePath;
    }

    private static File createTemporalFileFrom(Activity context, InputStream inputStream) throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];

            targetFile = createTemporalFile(context);
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    private static File createTemporalFile(Activity context) {
        return new File(getInternalAppDir(context), "temp");
    }

}
