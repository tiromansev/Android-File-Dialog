package com.tiromansev.filedialog.utils;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.text.DecimalFormat;

public class FileUtils {

    public static DocumentFile getDocumentFile(DocumentFile documentFile, String fileName) {
        DocumentFile result = documentFile.findFile(fileName);

        if (result == null) {
            return documentFile.createFile("*/*", fileName);
        }

        return result;
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

    public static String getFileName(Context context, Uri uri) {
        if (uri == null) {
            return "";
        }

        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile == null) {
            return "";
        }

        return documentFile.getName();
    }
}
