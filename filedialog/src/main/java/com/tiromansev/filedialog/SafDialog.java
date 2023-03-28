package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.tiromansev.filedialog.utils.DialogUtils;
import com.tiromansev.filedialog.utils.FileUtils;
import com.tiromansev.filedialog.utils.GuiUtils;

import java.lang.ref.WeakReference;

public class SafDialog implements IFileDialog {

    private final WeakReference<Activity> context;
    private FileDialogListener fileDialogListener = null;
    private FileNameDialogListener fileNameDialogListener = null;
    private ActivityResultLauncher<Intent> safLauncher;
    private int selectType = FILE_OPEN;
    private String mimeType;
    private String fileName;

    public SafDialog(Activity context) {
        this.context = new WeakReference<>(context);
    }

    public void setSafLauncher(ActivityResultLauncher<Intent> safLauncher) {
        this.safLauncher = safLauncher;
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
//        if (AppPrefs.showUseSafRationaleDialog().getValue()) {
//            AppPrefs.showUseSafRationaleDialog().setValue(false);
//            DialogUtils.showSimpleDialog(getContext(),
//                    getContext().getString(R.string.message_saf_use_rationale),
//                    this::launchSaf);
//            return;
//        }

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

        if (selectType == IFileDialog.FILE_SAVE) {
            if (fileNameDialogListener != null) {
                String fileName = FileUtils.getFileName(getContext(), uri);
                fileNameDialogListener.onFileResult(uri, fileName);
            }
        } else {
            if (fileDialogListener != null) {
                fileDialogListener.onFileResult(uri);
            }
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

        public Builder setMimeType(String mimeType) {
            SafDialog.this.setMimeType(mimeType);
            return this;
        }

        public IFileDialog build() {
            return SafDialog.this;
        }
    }
}
