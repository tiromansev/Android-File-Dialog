package com.tiromansev.filedialog;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.lang.ref.WeakReference;

public class SafFile {

    private Uri uri;
    private final WeakReference<Context> context;

    public SafFile(Context context, Uri uri) {
        this.context = new WeakReference<>(context);
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public DocumentFile getFile() {
        Context context = this.context.get();
        if (uri != null && context != null) {
            return DocumentFile.fromTreeUri(context, uri);
        }

        return null;
    }

    public String getName() {
        Context context = this.context.get();
        if (uri != null && context != null) {
            DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri);
            if (documentFile != null)
                return documentFile.getName();
        }

        return null;
    }

}
