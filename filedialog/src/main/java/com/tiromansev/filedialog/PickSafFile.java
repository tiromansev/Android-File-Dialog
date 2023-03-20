package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.tiromansev.filedialog.utils.DialogUtils;
import com.tiromansev.filedialog.utils.GuiUtils;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;

import io.reactivex.disposables.Disposable;

public class PickSafFile implements IFileDialog, FilesAdapter.ItemSelectListener {

    private int selectType = FILE_OPEN;
    private String[] filterFileExt;
    private final WeakReference<Activity> context;
    private RowItem selectedFile;
    private FileDialogListener fileDialogListener = null;
    private FileNameDialogListener fileNameDialogListener = null;
    private Comparator<RowItem> fileComparator;
    private HashMap<String, Integer> fileIcons = new HashMap<>();
    private boolean addModifiedDate = false;
    private AlertDialog openFileDialog;
    private AlertDialog saveFileDialog;
    private int fileImageId = R.mipmap.ic_file;
    private String fileName;
    private RecyclerView rlFiles;
    private ProgressBar pkProgress;
    private Disposable disposable;
    private String fileExt;
    private ActivityResultLauncher<Intent> safLauncher;

    public PickSafFile(Activity context) {
        this.context = new WeakReference<>(context);
        fileComparator = (leftItem, rightItem) -> leftItem.getTitle().compareToIgnoreCase(rightItem.getTitle());
    }

    public void setSafLauncher(ActivityResultLauncher<Intent> safLauncher) {
        this.safLauncher = safLauncher;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    @Override
    public void setAddModifiedDate(boolean add) {
        addModifiedDate = add;
    }

    public void setFileImageId(int fileImageId) {
        this.fileImageId = fileImageId;
    }

    public int getFileImageId() {
        return fileImageId;
    }

    public void setFileComparator(Comparator<RowItem> fileComparator) {
        this.fileComparator = fileComparator;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Activity getContext() {
        return context.get();
    }

    protected int getSelectType() {
        return selectType;
    }

    protected boolean isAddModifiedDate() {
        return addModifiedDate;
    }

    protected Comparator<RowItem> getFileComparator() {
        return fileComparator;
    }

    public String[] getFilterFileExt() {
        return filterFileExt;
    }

    public void setFileIcons(HashMap<String, Integer> fileIcons) {
        this.fileIcons = fileIcons;
    }

    @Override
    public void setFilterFileExt(String[] filterFileExt) {
        this.filterFileExt = filterFileExt;
    }

    public static Uri getBaseUri() {
        if (!TextUtils.isEmpty(AppPrefs.basePath().getValue())) {
            return Uri.parse(AppPrefs.basePath().getValue());
        }

        return null;
    }

    /*private void setBaseUri(Uri uri) {
        AppPrefs.basePath().setValue(uri.toString());
    }*/

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

    public HashMap<String, Integer> getFileIcons() {
        return fileIcons;
    }

    public void show() {
        if (AppPrefs.showUseSafRationaleDialog().getValue()) {
            AppPrefs.showUseSafRationaleDialog().setValue(false);
            DialogUtils.showSimpleDialog(getContext(),
                    getContext().getString(R.string.message_saf_use_rationale),
                    this::showDialog);
            return;
        }

        showDialog();
    }

    private void showDialog() {
        openSaf();

        /*if (getBaseUri() == null) {
            openSaf();
            return;
        }*/

        //handleSafAction();
    }

    private void openSaf() {
        if (safLauncher != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/vnd.ms-excel"); // TODO: 20/03/2023
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            intent.putExtra("android.content.extra.FANCY", true);
            intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
            GuiUtils.tryToStartLauncher(getContext(), safLauncher, intent);
        }
    }

    private void handleSafAction(Uri uri) {
        //SafFile safFile = new SafFile(getContext(), getBaseUri());
        //openFile(safFile);

        if (uri == null) {
            GuiUtils.showMessage(getContext(), R.string.message_file_must_be_selected);
            return;
        }

        if (fileDialogListener != null) {
            fileDialogListener.onFileResult(uri);
        }
        //openFileDialog.dismiss();
        unsubscribe();
    }

    public void handleSafLauncherResult(Intent data) {
        if (fileDialogListener == null && fileNameDialogListener == null) {
            return;
        }
        if (data != null) {
            Uri uri = data.getData();
            /*getContext().getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);*/
            //setBaseUri(uri);
            handleSafAction(uri);
        }
    }

    /*private void openFile(SafFile safFile) {
        AlertDialog.Builder dialogBuilder = createFileOpenDialog();
        dialogBuilder.setPositiveButton(R.string.caption_ok, null);

        openFileDialog = dialogBuilder.create();
        openFileDialog.show();
        openFileDialog.setOnCancelListener(dialog -> unsubscribe());
        openFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                view -> {
                    if (selectedFile == null) {
                        GuiUtils.showMessage(getContext(), R.string.message_file_must_be_selected);
                        return;
                    }
                    if (fileDialogListener != null) {
                        fileDialogListener.onFileResult(selectedFile.getUri());
                    }
                    if (fileNameDialogListener != null) {
                        fileNameDialogListener.onFileResult(selectedFile.getUri(), selectedFile.getTitle());
                    }
                    openFileDialog.dismiss();
                    unsubscribe();
                }
        );

        *//*showProgress();
        addSubscription(fileManager.getFilesAsync(safFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(this::closeProgress)
                .subscribe(rowItems -> {
                    setFileList(rowItems);
                    closeProgress();
                }, throwable -> {
                    closeProgress();
                    String message = getContext().getString(R.string.message_get_dir_content_error);
                    GuiUtils.showMessage(getContext(), message + ": " + throwable.getLocalizedMessage());
                }));*//*
    }*/

    /*private void setFileList(List<RowItem> fileList) {
        FilesAdapter filesAdapter = new FilesAdapter(getContext(), fileList, this);
        if (rlFiles != null) {
            rlFiles.setAdapter(filesAdapter);
        }
    }*/

    private void addSubscription(Disposable disposable) {
        this.disposable = disposable;
    }

    private void unsubscribe() {
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    private void showProgress() {
        if (rlFiles != null) {
            rlFiles.setVisibility(View.GONE);
        }
        if (pkProgress != null) {
            pkProgress.setVisibility(View.VISIBLE);
        }
    }

    private void closeProgress() {
        if (rlFiles != null) {
            rlFiles.setVisibility(View.VISIBLE);
        }
        if (pkProgress != null) {
            pkProgress.setVisibility(View.GONE);
        }
    }

    /*private AlertDialog.Builder createFileOpenDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);

        LinearLayout titleLayout = new LinearLayout(getContext());
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.drawable.bottom_border);

        titleLayout.addView(createTitleLayout());
        titleLayout.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_title_background, getContext()));

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View mainView = inflater.inflate(R.layout.view_main, null);
        rlFiles = mainView.findViewById(R.id.rlFies);
        pkProgress = mainView.findViewById(R.id.pkProgress);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rlFiles.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                mLayoutManager.getOrientation());
        rlFiles.addItemDecoration(dividerItemDecoration);

        dialogBuilder.setCustomTitle(titleLayout);
        dialogBuilder.setView(mainView);
        dialogBuilder.setCancelable(true);
        return dialogBuilder;
    }*/

    /*private View createTitleLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View titleView = inflater.inflate(R.layout.view_file_dialog_title, null);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int dialogMargin = (int) getContext().getResources().getDimension(R.dimen.dialog_margin);
        int dialogBottomMargin = (int) getContext().getResources().getDimension(R.dimen.dialog_bottom_margin);
        titleParams.setMargins(dialogMargin, dialogMargin, dialogMargin, dialogBottomMargin);
        titleView.setLayoutParams(titleParams);

        ImageButton btnChangeFolder = titleView.findViewById(R.id.btnChangeFolder);
        btnChangeFolder.setOnClickListener(v -> changeDir());

        TextView tvTitle = titleView.findViewById(R.id.tvFileTitle);
        String selectFileCaption = new SafFile(getContext(), getBaseUri()).getName();
        tvTitle.setText(selectFileCaption);

        return titleView;
    }*/

    private void changeDir() {
        if (saveFileDialog != null) {
            saveFileDialog.dismiss();
        }
        if (openFileDialog != null) {
            openFileDialog.dismiss();
        }
        openSaf();
    }

    public static Builder create(Activity context) {
        return new PickSafFile(context).new Builder();
    }

    @Override
    public void onItemSelected(RowItem rowItem) {
        selectedFile = rowItem;
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
         * определяет нужно ли в окне диалога показывать дату модификации файлов
         *
         * @param add
         */
        public Builder setAddModifiedDate(boolean add) {
            PickSafFile.this.setAddModifiedDate(add);
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
         * устанавливает слушатель для выбора файла, который возвращает строковое значение абсолютного пути к выбранному файлу
         *
         * @param listener
         */
        public Builder setFileNameDialogListener(FileNameDialogListener listener) {
            PickSafFile.this.setFileNameDialogListener(listener);
            return this;
        }

        /**
         * устанавливает фильтр по mime типу для файлов в окне диалога
         *
         * @param filterFileExt массив mime типов
         */
        public Builder setFilterFileExt(String[] filterFileExt) {
            PickSafFile.this.setFilterFileExt(filterFileExt);
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

        /**
         * устанавливает отображаемое название расщирения файла (нередактируемый text view)
         *
         * @param fileExt
         */
        public Builder setFileExt(String fileExt) {
            PickSafFile.this.setFileExt(fileExt);
            return this;
        }


        /**
         * устанавливает дефолтное имя файла в поле ввода
         *
         * @param fileName
         */
        public Builder setFileName(String fileName) {
            PickSafFile.this.setFileName(fileName);
            return this;
        }

        /**
         * устанавливает сортировщик файлов
         *
         * @param fileComparator
         */
        public Builder setFileComparator(Comparator<RowItem> fileComparator) {
            PickSafFile.this.setFileComparator(fileComparator);
            return this;
        }

        public PickSafFile build() {
            return PickSafFile.this;
        }

    }
}
