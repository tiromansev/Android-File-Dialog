package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.tiromansev.filedialog.utils.FileUtils;

import java.lang.ref.WeakReference;

public class SafFile {

    private Uri uri;
    private WeakReference<Activity> activityRef;

    public SafFile(Activity context, Uri uri) {
        this.activityRef = new WeakReference<>(context);
        context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.uri = uri;
    }

    public String getFilePath() {
        Activity activity = activityRef.get();
        if (uri != null && activity != null) {
            return FileUtils.getFilePathFromUri(activity, uri);
        }

        return null;
    }

    public DocumentFile getFile() {
        Activity activity = activityRef.get();
        if (uri != null && activity != null) {
            return DocumentFile.fromTreeUri(activity, uri);
        }

        return null;
    }

    public String getName() {
        Activity activity = activityRef.get();
        if (uri != null && activity != null) {
            return DocumentFile.fromTreeUri(activity, uri).getName();
        }

        return null;
    }

}
