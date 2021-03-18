package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

import com.tiromansev.filedialog.utils.ColorUtils;
import com.tiromansev.filedialog.utils.DialogUtils;
import com.tiromansev.filedialog.utils.FileUtils;
import com.tiromansev.filedialog.utils.GuiUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.tiromansev.filedialog.BreadCrumbs.UNDEFINED_VALUE;

public class FileDialog implements IFileDialog {

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

    @Override
    public void setAddModifiedDate(boolean add) {
        addModifiedDate = add;
    }

    public void setFileImageId(int fileImageId) {
        this.fileImageId = fileImageId;
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

    private Uri getBaseUri() {
        if (!TextUtils.isEmpty(AppPrefs.basePath().getValue())) {
            return Uri.parse(AppPrefs.basePath().getValue());
        }

        return null;
    }

    private void setBaseUri(Uri uri) {
        AppPrefs.basePath().setValue(uri.toString());
    }

    public FileDialog(Activity context) {
        this.context = new WeakReference<>(context);
        fileComparator = (leftItem, rightItem) -> leftItem.getTitle().compareToIgnoreCase(rightItem.getTitle());
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
        getContext().startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
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
        EditText edtFileName = dialogView.findViewById(R.id.edtFileName);
        edtFileName.setText(fileName);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(R.string.caption_ok, null);
        edtFileName.requestFocus();

        saveFileDialog = dialogBuilder.create();

        saveFileDialog.show();
        saveFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                view -> {
                    if (fileDialogListener != null) {
                        String fileName = edtFileName.getText().toString();
                        if (TextUtils.isEmpty(fileName)) {
                            GuiUtils.showMessage(getContext(), R.string.message_file_name_is_empty);
                            return;
                        }
                        DocumentFile result = safFile.getFile().createFile("*/*", fileName);
                        if (result == null) {
                            GuiUtils.showMessage(getContext(), R.string.message_file_create_failed);
                            return;
                        }
                        fileDialogListener.onFileResult(result.getUri());
                        saveFileDialog.dismiss();
                    }
                }
        );
    }

    private void openFile(SafFile safFile) {
        List<RowItem> files = getFiles(safFile);
        if (files == null) {
            return;
        }

        class SimpleFileDialogOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int item) {
                ArrayAdapter<RowItem> adapter = (ArrayAdapter<RowItem>) ((AlertDialog) dialog).getListView().getAdapter();
                selectedFile = adapter.getItem(item);
                adapter.notifyDataSetChanged();
            }
        }

        AlertDialog.Builder dialogBuilder = createFileOpenDialog(
                files, new SimpleFileDialogOnClickListener());
        dialogBuilder.setPositiveButton(R.string.caption_ok, null);

        openFileDialog = dialogBuilder.create();
        ListView listView = openFileDialog.getListView();
        listView.setDivider(new ColorDrawable(getContext().getResources().getColor(R.color.button_focused_color_start))); // set color
        listView.setDividerHeight(1); // set height

        // Show directory chooser dialog
        openFileDialog.show();
        openFileDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                view -> {
                    if (fileDialogListener != null) {
                        if (selectedFile == null) {
                            GuiUtils.showMessage(getContext(), R.string.message_file_must_be_selected);
                            return;
                        }
                        fileDialogListener.onFileResult(selectedFile.getUri());
                        openFileDialog.dismiss();
                    }
                }
        );
    }

    private List<RowItem> getFiles(SafFile safFile) {
        List<RowItem> files = new ArrayList<>();

        try {
            for (DocumentFile file : safFile.getFile().listFiles()) {
                if (file.isFile()) {
                    boolean exclude = false;
                    if (filterFileExt != null && filterFileExt.length > 0) {
                        exclude = true;
                        for (String filter : filterFileExt) {
                            String name = file.getName();
                            if (name != null && name.endsWith(filter)) {
                                exclude = false;
                                break;
                            }
                        }
                    }
                    if (exclude) {
                        continue;
                    }
                    RowItem item = null;
                    String data = null;
                    if (addModifiedDate) {
                        data = FileUtils.size(file.length());
                        Date lastModDate = new Date(file.lastModified());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.ENGLISH);
                        data += dateFormat.format(lastModDate);
                    }
                    if (fileIcons.size() > 0) {
                        for (Map.Entry<String, Integer> entry : fileIcons.entrySet()) {
                            String name = file.getName();
                            if (name != null && name.endsWith(entry.getKey())) {
                                item = new RowItem(entry.getValue(), file.getName(), data, file.lastModified(), file.getUri());
                                files.add(item);
                                break;
                            }
                        }
                    }
                    if (item == null) {
                        item = new RowItem(fileImageId, file.getName(), data, file.lastModified(), file.getUri());
                        files.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            GuiUtils.showMessage(getContext(), R.string.message_get_dir_content_error);
            return null;
        }

        Collections.sort(files, fileComparator);
        return new ArrayList<>(files);
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

    private AlertDialog.Builder createFileOpenDialog(List<RowItem> listItems,
                                                     DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);

        LinearLayout titleLayout = new LinearLayout(getContext());
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.drawable.bottom_border);

        titleLayout.addView(createTitleLayout());
        titleLayout.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_title_background, getContext()));

        dialogBuilder.setCustomTitle(titleLayout);
        dialogBuilder.setSingleChoiceItems(createListAdapter(listItems), -1, onClickListener);
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

    private ArrayAdapter<RowItem> createListAdapter(List<RowItem> items) {
        return new ArrayAdapter<RowItem>(getContext(),
                R.layout.view_file_dialog_item, items) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                RowItem rowItem = getItem(position);
                RelativeLayout rlDirItem;

                LayoutInflater mInflater = (LayoutInflater) getContext()
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.view_file_dialog_item, null);
                    holder = new ViewHolder();
                    holder.txtTitle = convertView.findViewById(R.id.tvFileItem);
                    holder.txtData = convertView.findViewById(R.id.tvFileData);
                    holder.imageView = convertView.findViewById(R.id.ivFileImage);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                rlDirItem = convertView.findViewById(R.id.rlDirItem);
                holder.txtTitle.setText(rowItem.getTitle());
                holder.txtData.setText(rowItem.getData());
                holder.txtData.setVisibility(rowItem.getData() != null ? View.VISIBLE : View.GONE);
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                if (rowItem.getImageId() != UNDEFINED_VALUE) {
                    holder.imageView.setImageResource(rowItem.getImageId());
                } else {
                    holder.imageView.setImageBitmap(null);
                }
                if (selectedFile != null && rowItem.getTitle().equals(selectedFile.getTitle())) {
                    rlDirItem.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_title_background, getContext()));
                    holder.txtTitle.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_selected_dir_item_color, getContext()));
                    holder.txtData.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_selected_dir_item_color, getContext()));
                } else {
                    rlDirItem.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_background, getContext()));
                    holder.txtTitle.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_dir_item_color, getContext()));
                    holder.txtData.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_dir_item_color, getContext()));
                }

                return convertView;
            }

        };
    }

    public static Builder create(Activity context) {
        return new FileDialog(context).new Builder();
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
