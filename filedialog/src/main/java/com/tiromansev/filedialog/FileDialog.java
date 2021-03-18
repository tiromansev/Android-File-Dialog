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

    private int addDirectoryImageId = R.mipmap.ic_add_folder;
    private int browserDirectoryImageId = R.mipmap.ic_browser_folder;
    private int browserDirectoryLockImageId = R.mipmap.ic_browser_folder_lock;
    private int browserDirectoryUpImageId = R.mipmap.ic_browser_folder_up;
    private int fileImageId = R.mipmap.ic_file;
    private int sdStorageImageId = R.mipmap.ic_sd_storage;

    @Override
    public void setAddModifiedDate(boolean add) {
        addModifiedDate = add;
    }

    public void setAddDirectoryImageId(int addDirectoryImageId) {
        this.addDirectoryImageId = addDirectoryImageId;
    }

    public void setBrowserDirectoryImageId(int browserDirectoryImageId) {
        this.browserDirectoryImageId = browserDirectoryImageId;
    }

    public void setBrowserDirectoryLockImageId(int browserDirectoryLockImageId) {
        this.browserDirectoryLockImageId = browserDirectoryLockImageId;
    }

    public void setBrowserDirectoryUpImageId(int browserDirectoryUpImageId) {
        this.browserDirectoryUpImageId = browserDirectoryUpImageId;
    }

    public void setFileImageId(int fileImageId) {
        this.fileImageId = fileImageId;
    }

    public void setSdStorageImageId(int sdStorageImageId) {
        this.sdStorageImageId = sdStorageImageId;
    }

    public void setFileComparator(Comparator<RowItem> fileComparator) {
        this.fileComparator = fileComparator;
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
        if (!TextUtils.isEmpty(AppPrefs.initialPath().getValue())) {
            return Uri.parse(AppPrefs.initialPath().getValue());
        }

        return null;
    }

    private void setBaseUri(Uri uri) {
        AppPrefs.initialPath().setValue(uri.toString());
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

        String message = getContext().getString(R.string.message_selected_file_dialog_base_dir);
        message = String.format(message, new SafFile(getContext(), getBaseUri()).getName());
        DialogUtils.showQuestionDialog(getContext(),
                message,
                (dialog, which) -> handleSafAction(),
                (dialog, which) -> openSaf());
    }

    private void openSaf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        getContext().startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE);
    }

    private void handleSafAction() {
        SafFile safFile = new SafFile(getContext(), getBaseUri());

        if (selectType == FILE_OPEN) {
            choose(safFile);
        } else {
            fileDialogListener.onChosenDir(safFile);
        }
    }

    public void handleRequestResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (fileDialogListener == null) {
            return;
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            if (data != null) {
                Uri uri = data.getData();
                getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                setBaseUri(uri);
                handleSafAction();
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    // choose(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    // //////////////////////////////////////////////////////////////////////////////
    private void choose(SafFile safFile) {
        List<RowItem> files = getFiles(safFile);
        if (files == null) {
            return;
        }

        class SimpleFileDialogOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int item) {
                ArrayAdapter<RowItem> adapter = (ArrayAdapter<RowItem>) ((AlertDialog) dialog).getListView().getAdapter();
                selectedFile = (RowItem) adapter.getItem(item);
                adapter.notifyDataSetChanged();
            }
        }

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(
                files, new SimpleFileDialogOnClickListener());
        String okCaption = getContext().getResources().getString(R.string.caption_ok);

        dialogBuilder.setPositiveButton(
                okCaption,
                null);

        final AlertDialog dirsDialog = dialogBuilder.create();
        ListView listView = dirsDialog.getListView();
        listView.setDivider(new ColorDrawable(getContext().getResources().getColor(R.color.button_focused_color_start))); // set color
        listView.setDividerHeight(1); // set height

        // Show directory chooser dialog
        dirsDialog.show();
        dirsDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                view -> {
                    if (fileDialogListener != null) {
                            if (selectedFile == null) {
                                GuiUtils.showMessage(getContext(), R.string.message_file_must_be_selected);
                                return;
                            }
                            fileDialogListener.onChosenDir(new SafFile(getContext(), selectedFile.getUri()));
                            dirsDialog.dismiss();
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
                            if (file.getName().endsWith(filter)) {
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
                            if (file.getName().endsWith(entry.getKey())) {
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

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //// START DIALOG DEFINITION //////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private AlertDialog.Builder createDirectoryChooserDialog(
            List<RowItem> listItems,
            DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);

        // ///////////////////////////////////////////////////
        // Create View with folder path and entry text box //
        // ///////////////////////////////////////////////////
        LinearLayout titleLayout = new LinearLayout(getContext());
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.drawable.bottom_border);

        titleLayout.addView(createTitleLayout());
        titleLayout.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_title_background, getContext()));

        // ////////////////////////////////////////
        // Set Views and Finish Dialog builder //
        // ////////////////////////////////////////
        dialogBuilder.setCustomTitle(titleLayout);
        dialogBuilder.setTitle(R.string.title_select_file);
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

        TextView tvTitle = titleView.findViewById(R.id.tvFileTitle);
        String selectFileCaption = getContext().getResources().getString(R.string.title_select_file);
        tvTitle.setText(selectFileCaption);

        return titleView;
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

        public Builder setFileComparator(Comparator<RowItem> fileComparator) {
            FileDialog.this.setFileComparator(fileComparator);
            return this;
        }

        public FileDialog build() {
            return FileDialog.this;
        }

    }
}
