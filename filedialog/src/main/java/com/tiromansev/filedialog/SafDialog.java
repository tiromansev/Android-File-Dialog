package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.anggrayudi.storage.SimpleStorageHelper;
import com.tiromansev.filedialog.utils.FileUtils;
import com.tiromansev.filedialog.utils.GuiUtils;

import java.lang.ref.WeakReference;

public class SafDialog implements IFileDialog {

    public static final String TEXT_MIME = "text/plain";
    public static final String BINARY_MIME = "application/octet-stream";
    public static final String PDF_MIME = "application/pdf";
    public static final String EXCEL_MIME = "application/vnd.ms-excel";
    public static final String CSV_MIME = "text/plain";
    public static final String SQLITE_MIME = "application/x-sqlite3";
    public static final String ZIP_MIME = "application/zip";
    public static final String EXCELX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String[] EXCEL_FILE_MIMES = new String[]{EXCEL_MIME, EXCELX_MIME};

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
    private int selectType = FILE_OPEN;
    private String mimeType;
    private String[] mimeTypes = new String[]{};
    private String fileName;
    private final SimpleStorageHelper storageHelper;

    public SafDialog(AppCompatActivity context, SimpleStorageHelper storageHelper) {
        this.context = new WeakReference<>(context);
        this.storageHelper = storageHelper;

        this.storageHelper.setOnFileCreated((requestCode, file) -> {
            if (fileDialogListener == null && fileNameDialogListener == null) {
                return null;
            }
            handleSafAction(file.getUri());
            return null;
        });

        this.storageHelper.setOnFileSelected((requestCode, files) -> {
            if (fileDialogListener == null && fileNameDialogListener == null) {
                return null;
            }
            handleSafAction(files.get(0).getUri());
            return null;
        });

        this.storageHelper.setOnFolderSelected((requestCode, folder) -> {
            if (fileDialogListener == null && fileNameDialogListener == null) {
                return null;
            }
            handleSafAction(folder.getUri());
            return null;
        });
    }

    @Override
    public void setAddModifiedDate(boolean add) {
    }

    public void setMimeTypes(String[] mimeTypes) {
        this.mimeTypes = new String[mimeTypes.length];

        for (int i = 0; i < mimeTypes.length; i++) {
            String mimeType = mimeTypes[i];

            if (mimeType.equals("text/plain") || mimeType.equals("text/csv")) {
                mimeType = "text/comma-separated-values";
            }

            this.mimeTypes[i] = mimeType;
        }
    }

    public Activity getContext() {
        return context.get();
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

    @Override
    public void handleSafLauncherResult(Intent data) {

    }

    private void launchSaf() {
        switch (selectType) {
            case FILE_OPEN:
                if (mimeTypes.length > 0) {
                    storageHelper.openFilePicker(0, false, mimeTypes);
                } else {
                    storageHelper.openFilePicker(0, false, mimeType);
                }
                break;
            case FOLDER_CHOOSE:
                storageHelper.openFolderPicker();
                break;
            case FILE_SAVE:
                storageHelper.createFile(mimeType, fileName);
                break;
        }
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
            String fileName = FileUtils.getFileName(getContext(), safFile.getUri());
            if (TextUtils.isEmpty(fileName)) {
                GuiUtils.showMessage(getContext(), R.string.message_file_name_is_empty);
                return;
            }
            if (fileDialogListener != null)
                fileDialogListener.onFileResult(safFile.getUri());
            if (fileNameDialogListener != null)
                fileNameDialogListener.onFileResult(safFile.getUri(), fileName);
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
            String fileExt = getFileExt(fileName);
            if (mimeTypes.length > 0) {
                boolean isValid = false;
                for (String mimeType : mimeTypes) {
                    if (isValidMimeType(mimeType, fileExt)) {
                        isValid = true;
                        break;
                    }
                }
                if (!isValid) {
                    GuiUtils.showMessage(getContext(), R.string.message_wrong_file_ext);
                    return;
                }
            } else if (!isValidMimeType(mimeType, fileExt)) {
                GuiUtils.showMessage(getContext(), R.string.message_wrong_file_ext);
                return;
            }
            fileNameDialogListener.onFileResult(safFile.getUri(), fileName);
        }
    }

    private String getFileExt(String fileName) {
        String fileExt;

        int i = fileName.lastIndexOf('.');
        fileExt = fileName.substring(i + 1);

        return "." + fileExt;
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
                if (fileExt.equals(BACKUP_FILE_EXT) ||
                        fileExt.equals(BACKUP_FILE_EXT2) ||
                        fileExt.equals(PRINT_FORM_FILE_EXT)) {
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

    public static Builder create(AppCompatActivity context, SimpleStorageHelper storageHelper) {
        return new SafDialog(context, storageHelper).new Builder();
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

        public Builder setMimeType(String mimeType) {
            SafDialog.this.setMimeType(mimeType);
            return this;
        }

        public Builder setMimeTypes(String[] mimeTypes) {
            SafDialog.this.setMimeTypes(mimeTypes);
            return this;
        }

        public IFileDialog build() {
            return SafDialog.this;
        }
    }
}
