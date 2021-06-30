package com.tiromansev.filedialog;

import android.net.Uri;

public interface FileDialogListener {

    void onFileResult(Uri uri, String fileName);

}
