package com.tiromansev.filedialog;

import android.net.Uri;

public class RowItem {
    private final int imageId;
    private final String title;
    private final String data;
    private final long lastModified;
    private final Uri uri;

    public RowItem(int imageId,
                   String title,
                   String data,
                   long lastModified,
                   Uri uri) {
        this.imageId = imageId;
        this.title = title;
        this.data = data;
        this.lastModified = lastModified;
        this.uri = uri;
    }

    public int getImageId() {
        return imageId;
    }

    public String getTitle() {
        return title;
    }

    public String getData() {
        return data;
    }

    public long getLastModified() {
        return lastModified;
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return title;
    }
}
