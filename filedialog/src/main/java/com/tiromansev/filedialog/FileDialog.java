package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.tiromansev.filedialog.BreadCrumbs.UNDEFINED_VALUE;

public class FileDialog implements IFileDialog {

    private BreadCrumbs breadCrumbs;
    private int selectType = FILE_OPEN;

    private String[] filterFileExt;
    private Activity context;
    private String selectedFileName = "";
    private boolean canExplore = true;
    private List<String> rootDirList = new ArrayList<>();
    private String currentDir = "";
    private List<RowItem> subDirectories = null;
    private FileDialogListener fileDialogListener = null;
    private ArrayAdapter<RowItem> listAdapter = null;
    private HorizontalScrollView scrollView;
    private Comparator<RowItem> directoryComparator;
    private Comparator<RowItem> fileComparator;
    private boolean useOldFileDialog = false;
    private HashMap<String, Integer> fileIcons = new HashMap<>();
    private boolean addModifiedDate = false;

    private int addDirectoryImageId = R.mipmap.ic_add_folder;
    private int browserDirectoryImageId = R.mipmap.ic_browser_folder;
    private int browserDirectoryLockImageId = R.mipmap.ic_browser_folder_lock;
    private int browserDirectoryUpImageId = R.mipmap.ic_browser_folder_up;
    private int fileImageId = R.mipmap.ic_file;
    private int sdStorageImageId = R.mipmap.ic_sd_storage;
    private boolean showAddFolder = true;

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

    public void setDirectoryComparator(Comparator<RowItem> directoryComparator) {
        this.directoryComparator = directoryComparator;
    }

    public void setFileComparator(Comparator<RowItem> fileComparator) {
        this.fileComparator = fileComparator;
    }

    public void setUseOldFileDialog(boolean useOldFileDialog) {
        this.useOldFileDialog = useOldFileDialog;
    }

    public Activity getContext() {
        return context;
    }

    protected int getSelectType() {
        return selectType;
    }

    protected boolean isCanExplore() {
        return canExplore;
    }

    protected boolean isAddModifiedDate() {
        return addModifiedDate;
    }

    protected FileDialogListener getFileDialogListener() {
        return fileDialogListener;
    }

    protected Comparator<RowItem> getDirectoryComparator() {
        return directoryComparator;
    }

    protected Comparator<RowItem> getFileComparator() {
        return fileComparator;
    }

    public void setShowAddFolder(boolean showAddFolder) {
        this.showAddFolder = showAddFolder;
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

    public class RowItem {
        private final int imageId;
        private final String title;
        private final String data;
        private final long lastModified;

        public RowItem(int imageId, String title, String data, long lastModified) {
            this.imageId = imageId;
            this.title = title;
            this.data = data;
            this.lastModified = lastModified;
        }

        public int getImageId() {
            return imageId;
        }

        public String getTitle() {
            return title;
        }

        public String getData() {
            return data;
        }

        public long getLastModified() {
            return lastModified;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public FileDialog() {

    }

    public FileDialog(Activity context) {
        this.context = context;
        breadCrumbs = new BreadCrumbs(context);
        breadCrumbs.setItemClickListener(new BreadCrumbs.SelectItemListener() {
            @Override
            public boolean onItemSelect(String itemTag) {
                if (!itemTag.equals(currentDir)) {
                    String oldDir = currentDir;
                    //check we go to root dir
                    String rootDirContains = getRootDirContains(itemTag);
                    if (rootDirContains != null) {
                        currentDir = "";
                    } else if (itemTag.equals(String.valueOf(UNDEFINED_VALUE))) {
                        scrollView.setVisibility(View.GONE);
                        currentDir = "";
                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        currentDir = itemTag;
                    }
                    if (updateDirectory()) {
                        return true;
                    } else {
                        currentDir = oldDir;
                        return false;
                    }
                }
                return false;
            }
        });

        fileComparator = new Comparator<RowItem>() {
            @Override
            public int compare(RowItem leftItem, RowItem rightItem) {
                return leftItem.getTitle().compareToIgnoreCase(rightItem.getTitle());
            }
        };
        directoryComparator = new Comparator<RowItem>() {
            @Override
            public int compare(RowItem leftItem, RowItem rightItem) {
                return leftItem.getTitle().compareToIgnoreCase(rightItem.getTitle());
            }
        };

        scrollView = new HorizontalScrollView(this.context);
        int dialogMargin = (int) this.context.getResources().getDimension(R.dimen.dialog_margin);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(dialogMargin, 0, dialogMargin, 0);
        scrollView.setLayoutParams(llp);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        initRootDirList();
    }

    @Override
    public void setSelectType(int selectType) {
        this.selectType = selectType;
    }

    public void setCanExplore(boolean canExplore) {
        this.canExplore = canExplore;
    }

    @Override
    public void setFileDialogListener(FileDialogListener fileDialogListener) {
        this.fileDialogListener = fileDialogListener;
    }

    protected void excludeRoot(String excludedRoot, String existingRoot) {

    }

    private void initRootDirList() {
        rootDirList.clear();
        List<String> mountPoints = FileUtils.getMountPoints(context, selectType != FILE_OPEN, useOldFileDialog);
        boolean include;
        String path;
        for (int i = 0; i < mountPoints.size(); i++) {
            include = true;
            path = mountPoints.get(i);
            for (String mountPoint : rootDirList) {
                if (MountPoint.calcHash(mountPoint) == MountPoint.calcHash(path)) {
                    excludeRoot(path, mountPoint);
                    include = false;
                    break;
                }
            }
            if (include) {
                rootDirList.add(path);
            }
        }
    }

    public void show(String dir) {
        // Initial directory is sdcard directory
        File initDir = new File(dir);
        if ((dir.equals("")) || (!initDir.exists()))
            choose("");
        else {
            choose(dir);
        }
    }

    private void showRootDir() {
        if (subDirectories == null) {
            subDirectories = new ArrayList<>();
        }
        subDirectories.clear();
        for (String rootDir : rootDirList) {
            subDirectories.add(new RowItem(sdStorageImageId, new File(rootDir).getAbsolutePath(), null, 0L));
        }
    }

    private String getRootDir(String dir) {
        for (String rootDir : rootDirList) {
            if (rootDir.equals(dir)) {
                return rootDir;
            }
        }
        return null;
    }

    private String getRootDirContains(String dir) {
        for (String rootDir : rootDirList) {
            if (dir.length() < rootDir.length() && rootDir.contains(dir)) {
                return rootDir;
            }
        }
        return null;
    }

    private void chooseClick(String sel) {
        String mDirOld = currentDir;
        String rootDir;

        // Navigate into the sub-directory
        if (sel.equals("..")) {
            rootDir = getRootDir(currentDir);
            //если переходим в корень
            if (rootDir != null) {
                currentDir = "";
            } else {
                currentDir = new File(currentDir).getParent();
            }
        } else {
            rootDir = getRootDir(sel);
            //если корень
            if ((currentDir == null || currentDir.isEmpty()) && (rootDir != null)) {
                currentDir = rootDir;
            } else {
                currentDir += "/" + sel;
            }
        }
        selectedFileName = "";

        File newDir;
        try {
            newDir = new File(currentDir);
        } catch (Exception e) {
            e.printStackTrace();
            GuiUtils.showMessage(context, e.getLocalizedMessage());
            return;
        }

        if (newDir.isFile()) // If the selection is a regular file
        {
            currentDir = mDirOld;
            selectedFileName = sel;
        }

        updateDirectory();
    }

    // //////////////////////////////////////////////////////////////////////////////
    // choose(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    // //////////////////////////////////////////////////////////////////////////////
    private void choose(String dir) {
        File dirFile = new File(dir);

        if (!dirFile.exists() || !dirFile.isDirectory()) {
            showRootDir();
        } else {
            List<RowItem> directories = getDirectories(dir);
            if (directories == null) {
                return;
            }
            subDirectories = directories;
        }
        currentDir = dir;

        class SimpleFileDialogOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int item) {
                String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                chooseClick(sel);
            }
        }

        AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(
                subDirectories, new SimpleFileDialogOnClickListener());
        dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                breadCrumbs.detachFrom();
            }
        });
        String okCaption = context.getResources().getString(R.string.caption_ok);

        dialogBuilder.setPositiveButton(
                okCaption,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Current directory chosen
                        // Call registered listener supplied with the chosen
                        // directory
                        if (fileDialogListener != null) {
                            if (selectType == FILE_OPEN) {
                                if (selectedFileName.length() > 0) {
                                    fileDialogListener.onChosenDir(currentDir + "/" + selectedFileName);
                                } else {
                                    GuiUtils.showMessage(context, R.string.message_file_must_be_selected);
                                }
                            } else {
                                if (selectedFileName.length() > 0) {
                                    fileDialogListener.onChosenDir(currentDir);
                                } else {
                                    GuiUtils.showMessage(context, R.string.message_directory_must_be_selected);
                                }
                            }
                        }
                    }
                });

        final AlertDialog dirsDialog = dialogBuilder.create();
        ListView listView = dirsDialog.getListView();
        listView.setDivider(new ColorDrawable(context.getResources().getColor(R.color.button_focused_color_start))); // set color
        listView.setDividerHeight(1); // set height

        // Show directory chooser dialog
        dirsDialog.show();
        dirsDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (fileDialogListener != null) {
                            File parentDir = new File(currentDir);
                            if (selectType == FILE_OPEN) {
                                if (selectedFileName.length() == 0) {
                                    GuiUtils.showMessage(context, R.string.message_file_must_be_selected);
                                    return;
                                }
                                fileDialogListener.onChosenDir(currentDir + "/" + selectedFileName);
                                breadCrumbs.detachFrom();
                                dirsDialog.dismiss();
                            } else {
                                if (!parentDir.canWrite()) {
                                    GuiUtils.showMessage(context, R.string.message_write_permission_denied);
                                    return;
                                }
                                fileDialogListener.onChosenDir(currentDir);
                                breadCrumbs.detachFrom();
                                dirsDialog.dismiss();
                            }
                        }
                    }
                }
        );
    }

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        return !newDirFile.exists() && newDirFile.mkdir();
    }

    private List<RowItem> getDirectories(String dir) {
        List<RowItem> dirs = new ArrayList<>();
        List<RowItem> files = new ArrayList<>();
        List<RowItem> result;

        File dirFile = new File(dir);
        try {
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory() && canExplore) {
                    RowItem item = new RowItem(file.canWrite() ? browserDirectoryImageId :
                            browserDirectoryLockImageId, file.getName(), null, file.lastModified());
                    dirs.add(item);
                } else if (selectType == FILE_OPEN) {
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
                                item = new RowItem(entry.getValue(), file.getName(), data, file.lastModified());
                                files.add(item);
                                break;
                            }
                        }
                    }
                    if (item == null) {
                        item = new RowItem(fileImageId, file.getName(), data, file.lastModified());
                        files.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            GuiUtils.showMessage(context, R.string.message_get_dir_content_error);
            return null;
        }

        Collections.sort(dirs, directoryComparator);
        Collections.sort(files, fileComparator);

        result = new ArrayList<>();
        if (dirFile.getParentFile() != null && canExplore) {
            result.add(new RowItem(browserDirectoryUpImageId, "..", null, 0L));
        }
        result.addAll(dirs);
        result.addAll(files);
        return result;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //// START DIALOG DEFINITION //////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private AlertDialog.Builder createDirectoryChooserDialog(
            List<RowItem> listItems,
            DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);

        // ///////////////////////////////////////////////////
        // Create View with folder path and entry text box //
        // ///////////////////////////////////////////////////
        LinearLayout titleLayout = new LinearLayout(context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.drawable.bottom_border);

        titleLayout.addView(createTitleLayout());
        getBreadCrumb();
        titleLayout.addView(scrollView);
        titleLayout.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_title_background, context));

        // ////////////////////////////////////////
        // Set Views and Finish Dialog builder //
        // ////////////////////////////////////////
        dialogBuilder.setCustomTitle(titleLayout);
        if (selectType == FILE_OPEN)
            dialogBuilder.setTitle(R.string.title_select_file);
        if (selectType == FOLDER_CHOOSE)
            dialogBuilder.setTitle(R.string.caption_folder_select);
        listAdapter = createListAdapter(listItems);
        dialogBuilder.setSingleChoiceItems(listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(true);
        return dialogBuilder;
    }

    private View createTitleLayout() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View titleView = inflater.inflate(R.layout.view_file_dialog_title, null);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int dialogMargin = (int) context.getResources().getDimension(R.dimen.dialog_margin);
        int dialogBottomMargin = (int) context.getResources().getDimension(R.dimen.dialog_bottom_margin);
        titleParams.setMargins(dialogMargin, dialogMargin, dialogMargin, dialogBottomMargin);
        titleView.setLayoutParams(titleParams);

        TextView tvTitle = titleView.findViewById(R.id.tvFileTitle);
        ImageButton addFolder = titleView.findViewById(R.id.btnAddFolder);
        addFolder.setImageResource(addDirectoryImageId);
        final String newFolder = context.getResources().getString(R.string.caption_new_folder_name);
        final String failedToCreateFolder = context.getResources().getString(R.string.message_failed_to_create_folder);
        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.editStringDialog(context,
                        newFolder,
                        "",
                        new DialogUtils.StringValueListener() {
                            @Override
                            public void onStringValue(String value) {
                                // Create new directory
                                if (createSubDir(currentDir + "/" + value)) {
                                    currentDir += "/" + value;
                                    updateDirectory();
                                } else {
                                    GuiUtils.showMessage(context, failedToCreateFolder.concat(" '").concat(value).concat("'"));
                                }
                            }
                        });
            }
        });
        addFolder.setVisibility((selectType == FOLDER_CHOOSE) && showAddFolder ? View.VISIBLE : View.GONE);
        String selectFileCaption = context.getResources().getString(R.string.title_select_file);
        String folderSelectCaption = context.getResources().getString(R.string.caption_folder_select);

        switch (selectType) {
            case FOLDER_CHOOSE:
                tvTitle.setText(folderSelectCaption);
                break;
            default:
                tvTitle.setText(selectFileCaption);
                break;
        }

        return titleView;
    }

    private void getBreadCrumb() {
        if (canExplore) {
            String[] dirs = currentDir.split("/");
            scrollView.removeAllViews();
            scrollView.setVisibility(View.VISIBLE);
            breadCrumbs.attachTo(scrollView);
            breadCrumbs.clearItems();
            String tag = "";
            if (dirs.length > 0) {
                for (int i = 0; i < dirs.length; i++) {
                    if (!dirs[i].isEmpty()) {
                        if (tag.isEmpty()) {
                            breadCrumbs.addHomeItem(dirs[i]);
                            tag = tag + "/" + dirs[i];
                        } else {
                            tag = tag + "/" + dirs[i];
                            breadCrumbs.addItem(dirs[i], tag);
                        }
                    }
                }
            }
            scrollView.setVisibility(breadCrumbs.getItemCount() > 0 ? View.VISIBLE : View.GONE);
        } else {
            scrollView.setVisibility(View.GONE);
        }
    }

    private boolean updateDirectory() {
        if (currentDir == null || currentDir.isEmpty()) {
            showRootDir();
        } else {
            List<RowItem> directories = getDirectories(currentDir);
            if (directories == null) {
                return false;
            }
            subDirectories.clear();
            subDirectories.addAll(directories);
        }
        String fileName = "";
        // #scorch
        if (selectType == FILE_OPEN) {
            fileName = selectedFileName;
        }
        File file = new File(currentDir + "/" + fileName);
        if (!file.isFile()) {
            getBreadCrumb();
        }
        listAdapter.notifyDataSetChanged();
        return true;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtData;
    }

    private ArrayAdapter<RowItem> createListAdapter(List<RowItem> items) {
        return new ArrayAdapter<RowItem>(context,
                R.layout.view_file_dialog_item, items) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                RowItem rowItem = getItem(position);
                RelativeLayout rlDirItem;

                LayoutInflater mInflater = (LayoutInflater) context
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
                if (rowItem.getTitle().equals(selectedFileName)) {
                    rlDirItem.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_title_background, context));
                    holder.txtTitle.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_selected_dir_item_color, context));
                    holder.txtData.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_selected_dir_item_color, context));
                } else {
                    rlDirItem.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_background, context));
                    holder.txtTitle.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_dir_item_color, context));
                    holder.txtData.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_dir_item_color, context));
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

        public Builder setCanExplore(boolean mCanExplore) {
            FileDialog.this.canExplore = mCanExplore;
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

        public Builder setDirectoryComparator(Comparator<RowItem> directoryComparator) {
            FileDialog.this.setDirectoryComparator(directoryComparator);
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
