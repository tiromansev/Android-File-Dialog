package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tiromansev.filedialog.utils.ColorUtils;
import com.tiromansev.filedialog.utils.DialogUtils;
import com.tiromansev.filedialog.utils.FileUtils;
import com.tiromansev.filedialog.utils.GuiUtils;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class FileDialog implements IFileDialog, FilesAdapter.ItemSelectListener {

    public static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 9999;

    private int selectType = FILE_OPEN;
    private String[] filterFileExt;
    private final WeakReference<Activity> context;
    private RowItem selectedFile;
    private FileDialogListener fileDialogListener = null;
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
    private FileManager fileManager;
    private String fileExt;

    public FileDialog(Activity context) {
        this.context = new WeakReference<>(context);
        fileManager = new FileManager(this);
        fileComparator = (leftItem, rightItem) -> leftItem.getTitle().compareToIgnoreCase(rightItem.getTitle());
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

    protected FileDialogListener getFileDialogListener() {
        return fileDialogListener;
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

    private void setBaseUri(Uri uri) {
        AppPrefs.basePath().setValue(uri.toString());
    }

    @Override
    public void setSelectType(int selectType) {
        this.selectType = selectType;
    }

    @Override
    public void setFileDialogListener(FileDialogListener fileDialogListener) {
        this.fileDialogListener = fileDialogListener;
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
        if (getBaseUri() == null) {
            openSaf();
            return;
        }

        handleSafAction();
    }

    private void openSaf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);
        GuiUtils.tryToStartIntentFoResult(getContext(), intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
    }

    private void handleSafAction() {
        SafFile safFile = new SafFile(getContext(), getBaseUri());

        if (selectType == FILE_OPEN)
            openFile(safFile);
        else
            saveFile(safFile);
    }

    public void handleRequestResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (fileDialogListener == null) {
            return;
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            if (data != null) {
                Uri uri = data.getData();
                getContext().getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                setBaseUri(uri);
                handleSafAction();
            }
        }
    }

    private void saveFile(SafFile safFile) {
        AlertDialog.Builder dialogBuilder = createFileSaveDialog();
        LinearLayout dialogView =
                (LinearLayout) getContext().getLayoutInflater().inflate(R.layout.view_save_file, null);
        RelativeLayout rlFileName = dialogView.findViewById(R.id.rlFileName);
        rlFileName.setVisibility(selectType == FOLDER_CHOOSE ? View.GONE : View.VISIBLE);
        EditText edtFileName = dialogView.findViewById(R.id.edtFileName);
        TextView edtExtension = dialogView.findViewById(R.id.edtExtension);
        edtExtension.setVisibility(TextUtils.isEmpty(fileExt) ?  View.GONE : View.VISIBLE);
        edtExtension.setText(fileExt);
        edtFileName.setText(fileName);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.caption_ok, null);
        edtFileName.requestFocus();

        saveFileDialog = dialogBuilder.create();

        saveFileDialog.show();
        saveFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                view -> {
                    if (fileDialogListener != null) {
                        if (selectType == FOLDER_CHOOSE) {
                            fileDialogListener.onFileResult(getBaseUri(), null);
                        } else {
                            String resultFileExt = TextUtils.isEmpty(fileExt) ? "" : fileExt;
                            String fileName = edtFileName.getText().toString();
                            if (TextUtils.isEmpty(fileName)) {
                                GuiUtils.showMessage(getContext(), R.string.message_file_name_is_empty);
                                return;
                            }
                            DocumentFile result = FileUtils.getDocumentFile(safFile.getFile(), fileName + resultFileExt);
                            if (result == null) {
                                GuiUtils.showMessage(getContext(), R.string.message_file_create_failed);
                                return;
                            }
                            fileDialogListener.onFileResult(result.getUri(), fileName + resultFileExt);
                        }
                        saveFileDialog.dismiss();
                    }
                }
        );
    }

    private void openFile(SafFile safFile) {
        AlertDialog.Builder dialogBuilder = createFileOpenDialog();
        dialogBuilder.setPositiveButton(R.string.caption_ok, null);

        openFileDialog = dialogBuilder.create();
        openFileDialog.show();
        openFileDialog.setOnCancelListener(dialog -> unsubscribe());
        openFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                view -> {
                    if (fileDialogListener != null) {
                        if (selectedFile == null) {
                            GuiUtils.showMessage(getContext(), R.string.message_file_must_be_selected);
                            return;
                        }
                        fileDialogListener.onFileResult(selectedFile.getUri(), null);
                        openFileDialog.dismiss();
                        unsubscribe();
                    }
                }
        );

        showProgress();
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
                }));
    }

    private void setFileList(List<RowItem> fileList) {
        FilesAdapter filesAdapter = new FilesAdapter(getContext(), fileList, this);
        if (rlFiles != null) {
            rlFiles.setAdapter(filesAdapter);
        }
    }

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

    private AlertDialog.Builder createFileSaveDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);

        LinearLayout titleLayout = new LinearLayout(getContext());
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.drawable.bottom_border);

        titleLayout.addView(createTitleLayout());
        titleLayout.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_title_background, getContext()));

        dialogBuilder.setCustomTitle(titleLayout);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setTitle(null);
        return dialogBuilder;
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

    private AlertDialog.Builder createFileOpenDialog() {
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
    }

    private View createTitleLayout() {
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
    }

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
        return new FileDialog(context).new Builder();
    }

    @Override
    public void onItemSelected(RowItem rowItem) {
        selectedFile = rowItem;
    }

    public class Builder {

        public Builder() {

        }

        public Builder setSelectType(int selectType) {
            FileDialog.this.setSelectType(selectType);
            return this;
        }

        public Builder setAddModifiedDate(boolean add) {
            FileDialog.this.setAddModifiedDate(add);
            return this;
        }

        public Builder setFileDialogListener(FileDialogListener listener) {
            FileDialog.this.setFileDialogListener(listener);
            return this;
        }

        public Builder setFilterFileExt(String[] filterFileExt) {
            FileDialog.this.setFilterFileExt(filterFileExt);
            return this;
        }

        public Builder setFileExt(String fileExt) {
            FileDialog.this.setFileExt(fileExt);
            return this;
        }

        public Builder setFileName(String fileName) {
            FileDialog.this.setFileName(fileName);
            return this;
        }

        public Builder setFileComparator(Comparator<RowItem> fileComparator) {
            FileDialog.this.setFileComparator(fileComparator);
            return this;
        }

        public FileDialog build() {
            return FileDialog.this;
        }

    }
}
