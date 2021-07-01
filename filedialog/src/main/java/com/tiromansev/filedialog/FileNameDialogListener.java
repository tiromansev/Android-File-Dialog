package com.tiromansev.filedialog;

import android.net.Uri;

public interface FileNameDialogListener {

    void onFileResult(Uri uri, String fileName);

}
