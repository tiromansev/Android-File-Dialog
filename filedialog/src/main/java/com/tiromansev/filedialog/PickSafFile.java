package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.tiromansev.filedialog.utils.DialogUtils;
import com.tiromansev.filedialog.utils.GuiUtils;

import java.lang.ref.WeakReference;

public class PickSafFile implements IFileDialog {

    private final WeakReference<Activity> context;
    private FileDialogListener fileDialogListener = null;
    private String mimeType;
    private ActivityResultLauncher<Intent> safLauncher;
    private int selectType = FILE_OPEN;

    public PickSafFile(Activity context) {
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

    public void show() {
        if (AppPrefs.showUseSafRationaleDialog().getValue()) {
            AppPrefs.showUseSafRationaleDialog().setValue(false);
            DialogUtils.showSimpleDialog(getContext(),
                    getContext().getString(R.string.message_saf_use_rationale),
                    this::openSaf);
            return;
        }

        openSaf();
    }

    private void openSaf() {
        if (safLauncher != null) {
            switch (selectType) {
                case FILE_OPEN:
                    GuiUtils.tryToStartLauncher(getContext(), safLauncher, openFileIntent());
                    break;
                case FOLDER_CHOOSE:
                    GuiUtils.tryToStartLauncher(getContext(), safLauncher, chooseFolderIntent());
                    break;
            }
        }
    }

    @NonNull
    private Intent chooseFolderIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
        return intent;
    }

    @NonNull
    private Intent openFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
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

        if (fileDialogListener != null) {
            fileDialogListener.onFileResult(uri);
        }
    }

    public void handleSafLauncherResult(Intent data) {
        if (fileDialogListener == null) {
            return;
        }

        if (data != null) {
            Uri uri = data.getData();
            handleSafAction(uri);
        }
    }

    public static Builder create(Activity context) {
        return new PickSafFile(context).new Builder();
    }

    public class Builder {

        public Builder() {
        }

        /**
         * устанавливает тип диалога
         *
         * @param selectType может иметь два значения
         *                   FILE_OPEN - открываем диалог выбора файла
         *                   FOLDER_CHOOSE - открываем диалог выбора папки
         */
        public Builder setSelectType(int selectType) {
            PickSafFile.this.setSelectType(selectType);
            return this;
        }

        /**
         * устанавливает слушатель для выбора файла/папки, который возвращает строковое значение абсолютного пути к выбранной папки/файлу
         *
         * @param listener
         */
        public Builder setFileDialogListener(FileDialogListener listener) {
            PickSafFile.this.setFileDialogListener(listener);
            return this;
        }

        /**
         * устанавливает обработчик возврата в активити при вызове SAF диалога выбора папки для записи
         * (требования гугла)
         *
         * @param safLauncher
         */
        public Builder setSafLauncher(ActivityResultLauncher<Intent> safLauncher) {
            PickSafFile.this.setSafLauncher(safLauncher);
            return this;
        }

        public Builder setMimeType(String mimeType) {
            PickSafFile.this.setMimeType(mimeType);
            return this;
        }

        public IFileDialog build() {
            return PickSafFile.this;
        }
    }
}
