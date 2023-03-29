package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.tiromansev.filedialog.utils.FileUtils;
import com.tiromansev.filedialog.utils.GuiUtils;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

public class SafDialog implements IFileDialog {

    public static final String TEXT_MIME = "text/plain";
    public static final String BINARY_MIME = "application/octet-stream";
    public static final String PDF_MIME = "application/pdf";
    public static final String EXCEL_MIME = "application/vnd.ms-excel";
    public static final String CSV_MIME = "text/plain";
    public static final String SQLITE_MIME = "application/x-sqlite3";
    public static final String ZIP_MIME = "application/zip";

    public static final String EXCEL_FILE_EXT = ".xls";
    public static final String CSV_FILE_EXT = ".csv";
    public static final String EXCELX_FILE_EXT = ".xlsx";
    public static final String BACKUP_FILE_EXT = ".bp";
    public static final String BACKUP_FILE_EXT2 = ".bp2";
    public static final String PRINT_FORM_FILE_EXT = ".pf";
    public static final String ZIP_FILE_EXT = ".zip";
    public static final String PDF_FILE_EXT = ".pdf";

    private final WeakReference<Activity> context;
    private FileDialogListener fileDialogListener = null;
    private FileNameDialogListener fileNameDialogListener = null;
    private ActivityResultLauncher<Intent> safLauncher;
    private int selectType = FILE_OPEN;
    private String mimeType;
    private String fileName;
    private String fileExt;

    public SafDialog(Activity context) {
        this.context = new WeakReference<>(context);
    }

    public void setSafLauncher(ActivityResultLauncher<Intent> safLauncher) {
        this.safLauncher = safLauncher;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    @Override
    public void setAddModifiedDate(boolean add) {
    }

    public Activity getContext() {
        return context.get();
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        //workaround - with text/plain and text/csv csv files in document picker are grayed out
        if (mimeType.equals("text/plain") || mimeType.equals("text/csv")) {
            this.mimeType = "text/comma-separated-values";
            return;
        }

        this.mimeType = mimeType;
    }

    @Override
    public void setFilterFileExt(String[] filterFileExt) {
    }

    @Override
    public void setSelectType(int selectType) {
        this.selectType = selectType;
    }

    @Override
    public void setFileDialogListener(FileDialogListener fileDialogListener) {
        this.fileDialogListener = fileDialogListener;
    }

    public void setFileNameDialogListener(FileNameDialogListener fileNameDialogListener) {
        this.fileNameDialogListener = fileNameDialogListener;
    }

    public void show() {
        launchSaf();
    }

    private void launchSaf() {
        if (safLauncher == null) {
            return;
        }

        switch (selectType) {
            case FILE_OPEN:
                GuiUtils.tryToStartLauncher(getContext(), safLauncher, openFileIntent());
                break;
            case FOLDER_CHOOSE:
                GuiUtils.tryToStartLauncher(getContext(), safLauncher, chooseFolderIntent());
                break;
            case FILE_SAVE:
                GuiUtils.tryToStartLauncher(getContext(), safLauncher, createFileIntent());
                break;
        }
    }

    @NonNull
    private Intent chooseFolderIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        return applyCommonSettings(intent);
    }

    @NonNull
    private Intent openFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        return applyCommonSettings(intent);
    }

    @NonNull
    private Intent createFileIntent() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        return applyCommonSettings(intent);
    }

    private Intent applyCommonSettings(Intent intent) {
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
        return intent;
    }

    private void handleSafAction(Uri uri) {
        if (uri == null) {
            GuiUtils.showMessage(getContext(), R.string.message_file_must_be_selected);
            return;
        }
        SafFile safFile = new SafFile(getContext(), uri);

        if (selectType == FILE_OPEN)
            openFile(safFile);
        else
            saveFile(safFile);
    }

    private void saveFile(SafFile safFile) {
        if (selectType == FOLDER_CHOOSE) {
            if (fileDialogListener != null)
                fileDialogListener.onFileResult(safFile.getUri());
            if (fileNameDialogListener != null)
                fileNameDialogListener.onFileResult(safFile.getUri(), null);
        } else {
            String resultFileExt = TextUtils.isEmpty(fileExt) ? "" : fileExt;
            String fileName = FileUtils.getFileName(getContext(), safFile.getUri());
            if (TextUtils.isEmpty(fileName)) {
                GuiUtils.showMessage(getContext(), R.string.message_file_name_is_empty);
                return;
            }
            DocumentFile result = DocumentFile.fromSingleUri(getContext(), safFile.getUri());
            try {
                safFile.setUri(DocumentsContract.renameDocument(getContext().getContentResolver(),
                        result.getUri(), fileName + resultFileExt));
            } catch (FileNotFoundException e) {
                GuiUtils.showMessage(getContext(), R.string.message_file_create_failed);
                return;
            }
            if (fileDialogListener != null)
                fileDialogListener.onFileResult(result.getUri());
            if (fileNameDialogListener != null)
                fileNameDialogListener.onFileResult(result.getUri(), fileName + resultFileExt);
        }
    }

    private void openFile(SafFile safFile) {
        if (fileDialogListener != null) {
            fileDialogListener.onFileResult(safFile.getUri());
        }
        if (fileNameDialogListener != null) {
            String fileName = FileUtils.getFileName(getContext(), safFile.getUri());
            if (TextUtils.isEmpty(fileName)) {
                GuiUtils.showMessage(getContext(), R.string.message_file_name_is_empty);
                return;
            }
            if (!isValidMimeType(mimeType, getFileExt(fileName))) {
                GuiUtils.showMessage(getContext(), R.string.message_wrong_file_ext);
                return;
            }
            fileNameDialogListener.onFileResult(safFile.getUri(), fileName);
        }
    }

    public void handleSafLauncherResult(Intent data) {
        if (fileDialogListener == null && fileNameDialogListener == null) {
            return;
        }

        if (data != null) {
            Uri uri = data.getData();
            handleSafAction(uri);
        }
    }

    private String getFileExt(String fileName) {
        String fileExt;

        int i = fileName.lastIndexOf('.');
        fileExt = fileName.substring(i + 1);

        return fileExt;
    }

    private boolean isValidMimeType(String mimeType, String fileExt) {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        switch (mimeType) {
            case CSV_MIME:
                if (fileExt.equals(CSV_FILE_EXT)) {
                    return true;
                }
                break;
            case EXCEL_MIME:
                if (fileExt.equals(EXCEL_FILE_EXT) ||
                        fileExt.equals(EXCELX_FILE_EXT)) {
                    return true;
                }
                break;
            case ZIP_MIME:
                if (fileExt.equals(ZIP_FILE_EXT)) {
                    return true;
                }
                break;
            case SQLITE_MIME:
                if (fileExt.equals(BACKUP_FILE_EXT)) {
                    return true;
                }
                break;
            case BINARY_MIME:
                if (fileExt.equals(BACKUP_FILE_EXT) || fileExt.equals(BACKUP_FILE_EXT2)) {
                    return true;
                }
                break;
            case PDF_MIME:
                if (fileExt.equals(PDF_FILE_EXT)) {
                    return true;
                }
                break;
        }

        return false;
    }

    public static Builder create(Activity context) {
        return new SafDialog(context).new Builder();
    }

    public class Builder {

        public Builder() {
        }

        /**
         * устанавливает дефолтное имя файла в поле ввода
         *
         * @param fileName
         */
        public Builder setFileName(String fileName) {
            SafDialog.this.fileName = fileName;
            return this;
        }

        /**
         * устанавливает тип диалога
         *
         * @param selectType может иметь два значения
         *                   FILE_OPEN - открываем диалог выбора файла
         *                   FOLDER_CHOOSE - открываем диалог выбора папки
         */
        public Builder setSelectType(int selectType) {
            SafDialog.this.setSelectType(selectType);
            return this;
        }

        /**
         * устанавливает слушатель для выбора файла/папки, который возвращает строковое значение абсолютного пути к выбранной папки/файлу
         *
         * @param listener
         */
        public Builder setFileDialogListener(FileDialogListener listener) {
            SafDialog.this.setFileDialogListener(listener);
            return this;
        }

        /**
         * устанавливает слушатель для выбора файла, который возвращает строковое значение абсолютного пути к выбранному файлу
         *
         * @param listener
         */
        public SafDialog.Builder setFileNameDialogListener(FileNameDialogListener listener) {
            SafDialog.this.setFileNameDialogListener(listener);
            return this;
        }

        /**
         * устанавливает обработчик возврата в активити при вызове SAF диалога выбора папки для записи
         * (требования гугла)
         *
         * @param safLauncher
         */
        public Builder setSafLauncher(ActivityResultLauncher<Intent> safLauncher) {
            SafDialog.this.setSafLauncher(safLauncher);
            return this;
        }

        public Builder setFileExt(String fileExt) {
            SafDialog.this.setFileExt(fileExt);
            return this;
        }

        public Builder setMimeType(String mimeType) {
            SafDialog.this.setMimeType(mimeType);
            return this;
        }

        public IFileDialog build() {
            return SafDialog.this;
        }
    }
}
