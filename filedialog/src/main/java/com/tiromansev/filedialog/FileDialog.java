package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.tiromansev.filedialog.BreadCrumbs.UNDEFINED_VALUE;

public class FileDialog {

    private BreadCrumbs breadCrumbs;
	private final int fileOpen = 0;
	private final int fileSave = 1;
	private final int folderChoose = 2;
	private int selectType = fileSave;
	private String mSdcardDirectory = "";
	private final Activity mContext;
	public String defaultFileName = "";
	private String selectedFileName = defaultFileName;

	public static final String FILE_OPEN = "fileOpen";
	public static final String FILE_SAVE = "fileSave";
	public static final String FOLDER_CHOOSE = "folderChoose";

    private int mFileType;
    private boolean mCanExplore = true;
    private boolean useOldFileDialog = false;

    private List<String> rootDirList = new ArrayList<>();

	private String mDir = "";
	private List<RowItem> mSubdirs = null;
	private SimpleFileDialogListener mSimplefiledialoglistener = null;
	private ArrayAdapter<RowItem> mListadapter = null;
    private HorizontalScrollView scrollView;

    // ////////////////////////////////////////////////////
	// Callback interface for selected directory
	// ////////////////////////////////////////////////////
	public interface SimpleFileDialogListener {
		void onChosenDir(String chosenDir);
	}

    public class RowItem {
        private final int imageId;
        private final String title;

        public RowItem(int imageId, String title) {
            this.imageId = imageId;
            this.title = title;
        }
        public int getImageId() {
            return imageId;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

	public FileDialog(Activity context,
                      String fileSelectType,
                      boolean canExplore,
                      boolean useOldFileDialog,
                      SimpleFileDialogListener simpleFileDialogListener) {
        switch (fileSelectType) {
            case FILE_OPEN:
                selectType = fileOpen;
                break;
            case FILE_SAVE:
                selectType = fileSave;
                break;
            case FOLDER_CHOOSE:
                selectType = folderChoose;
                break;
            default:
                selectType = fileOpen;
                break;
        }
        mCanExplore = canExplore;
        mContext = context;
        mSimplefiledialoglistener = simpleFileDialogListener;
        this.useOldFileDialog = useOldFileDialog;

        breadCrumbs = new BreadCrumbs(context);
        breadCrumbs.setItemClickListener(new BreadCrumbs.SelectItemListener() {
            @Override
            public boolean onItemSelect(String itemTag) {
                if (!itemTag.equals(mDir)) {
                    if (itemTag.equals(String.valueOf(UNDEFINED_VALUE))) {
                        scrollView.setVisibility(View.GONE);
                        mDir = "";
                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        mDir = itemTag;
                    }
                    updateDirectory();
                    return true;
                }
                return false;
            }
        });

        scrollView = new HorizontalScrollView(mContext);
        int dialogMargin = (int) mContext.getResources().getDimension(R.dimen.dialog_margin);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llp.setMargins(dialogMargin, 0, dialogMargin, 0);
        scrollView.setLayoutParams(llp);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        initRootDirList();
	}

    private void initRootDirList() {
        rootDirList.clear();
        List<String> mountPoints = FileUtils.getMountPoints(mContext, selectType != fileOpen, useOldFileDialog);
        boolean include;
        String path;
        for (int i = 0; i < mountPoints.size(); i++) {
            include = true;
            path = mountPoints.get(i);
            for (String mountPoint: rootDirList) {
                if (MountPoint.calcHash(mountPoint) == MountPoint.calcHash(path)) {
                    include = false;
                    break;
                }
            }
            if (include) {
                rootDirList.add(path);
            }
        }
    }

	// /////////////////////////////////////////////////////////////////////
	// choose() - load directory chooser dialog for initial
	// default sdcard directory
	// /////////////////////////////////////////////////////////////////////
	public void chooseFileOrDir(String dir) {
		// Initial directory is sdcard directory
        File initDir = new File(dir);
        if ((dir.equals("")) || (!initDir.exists()))
            choose(mSdcardDirectory);
        else {
            choose(dir);
        }
	}

    private void showRootDir() {
        if (mSubdirs == null) {
            mSubdirs = new ArrayList<>();
        }
        mSubdirs.clear();
        for (String rootDir: rootDirList) {
            mSubdirs.add(new RowItem(R.mipmap.ic_sd_storage, new File(rootDir).getAbsolutePath()));
        }
    }

    private String getRootDir(String dir) {
        for (String rootDir: rootDirList) {
            if (rootDir.equals(dir)) {
                return rootDir;
            }
        }
        return null;
    }

    private void chooseClick(String sel) {
        String mDirOld = mDir;
        String rootDir;

        // Navigate into the sub-directory
        if (sel.equals("..")) {
            rootDir = getRootDir(mDir);
            //если переходим в корень
            if (rootDir != null) {
                mDir = "";
            }
            else {
                mDir = new File(mDir).getParent();
            }
        } else {
            rootDir = getRootDir(sel);
            //если корень
            if ((mDir == null || mDir.isEmpty()) && (rootDir != null)) {
                mDir = rootDir;
            }
            else {
                mDir += "/" + sel;
            }
        }
        selectedFileName = defaultFileName;

        File newDir;
        try {
            newDir = new File(mDir);
        } catch (Exception e) {
            e.printStackTrace();
            GuiUtils.showMessage(mContext, e.getLocalizedMessage());
            return;
        }

        if (newDir.isFile()) // If the selection is a regular file
        {
            mDir = mDirOld;
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
        mDir = dir;

		if (!dirFile.exists() || !dirFile.isDirectory()) {
            showRootDir();
		}
        else {
            mSubdirs = getDirectories(dir);
        }

		class SimpleFileDialogOnClickListener implements DialogInterface.OnClickListener {
			public void onClick(DialogInterface dialog, int item) {
                String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
				chooseClick(sel);
			}
		}

		AlertDialog.Builder dialogBuilder = createDirectoryChooserDialog(
            mSubdirs, new SimpleFileDialogOnClickListener());
        String okCaption = mContext.getResources().getString(R.string.caption_ok);

		dialogBuilder.setPositiveButton(
                okCaption,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Current directory chosen
                        // Call registered listener supplied with the chosen
                        // directory
                        if (mSimplefiledialoglistener != null) {
                            if (selectType == fileOpen || selectType == fileSave) {
                                if (selectedFileName.length() > 0) {
                                    mSimplefiledialoglistener.onChosenDir(mDir + "/" + selectedFileName);
                                }
                                else {
                                    GuiUtils.showMessage(mContext, R.string.message_file_must_be_selected);
                                }
                            } else {
                                mSimplefiledialoglistener.onChosenDir(mDir);
                            }
                        }
                    }
                });

		final AlertDialog dirsDialog = dialogBuilder.create();

		// Show directory chooser dialog
		dirsDialog.show();
        dirsDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSimplefiledialoglistener != null) {
                            File parentDir = new File(mDir);
                            if (selectType == fileOpen || selectType == fileSave) {
                                if (selectedFileName.length() == 0) {
                                    GuiUtils.showMessage(mContext, R.string.message_file_must_be_selected);
                                    return;
                                }
                                if (selectType == fileSave && !parentDir.canWrite()) {
                                    GuiUtils.showMessage(mContext, R.string.message_write_permission_denied);
                                    return;
                                }
                                mSimplefiledialoglistener.onChosenDir(mDir + "/" + selectedFileName);
                                dirsDialog.dismiss();
                            } else {
                                if (!parentDir.canWrite()) {
                                    GuiUtils.showMessage(mContext, R.string.message_write_permission_denied);
                                    return;
                                }
                                mSimplefiledialoglistener.onChosenDir(mDir);
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
        List<RowItem> result = new ArrayList<>();

		try {
			File dirFile = new File(dir);

			// if directory is not the base sd card directory add ".." for going
			// up one directory
			if (!mDir.equals(mSdcardDirectory) && mCanExplore) {
                RowItem item = new RowItem(R.mipmap.ic_browser_folder_up, "..");
                dirs.add(item);
            }

			if (!dirFile.exists() || !dirFile.isDirectory()) {
				return dirs;
			}

			for (File file : dirFile.listFiles()) {
				if (file.isDirectory() && mCanExplore) {
					// Add "/" to directory names to identify them in the list
                    RowItem item = new RowItem(file.canWrite() ? R.mipmap.ic_browser_folder :
                            R.mipmap.ic_browser_folder_lock, file.getName());
                    dirs.add(item);
				} else if (selectType == fileSave || selectType == fileOpen) {
					// Add file names to the list if we are doing a file save or
					// file open operation
                    RowItem item = new RowItem(R.mipmap.ic_file, file.getName());
                    files.add(item);
				}
			}
		} catch (Exception e) {
            e.printStackTrace();
            GuiUtils.showMessage(mContext, R.string.message_get_dir_content_error);
		}

		Collections.sort(dirs, new Comparator<RowItem>() {
            @Override
            public int compare(RowItem rowItem, RowItem t1) {
                return rowItem.getTitle().compareToIgnoreCase(t1.getTitle());
            }
        });

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
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);

        // ///////////////////////////////////////////////////
        // Create View with folder path and entry text box //
        // ///////////////////////////////////////////////////
        LinearLayout titleLayout = new LinearLayout(mContext);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.drawable.bottom_border);

        titleLayout.addView(createTitlelayout());
        getBreadCrumb();
        titleLayout.addView(scrollView);
        titleLayout.setBackgroundColor(mContext.getResources().getColor(R.color.file_dialog_color));

		// ////////////////////////////////////////
		// Set Views and Finish Dialog builder //
		// ////////////////////////////////////////
        dialogBuilder.setCustomTitle(titleLayout);
		if (selectType == fileOpen)
			dialogBuilder.setTitle(R.string.title_select_file);
		if (selectType == fileSave)
			dialogBuilder.setTitle(R.string.caption_save_as);
		if (selectType == folderChoose)
			dialogBuilder.setTitle(R.string.caption_folder_select);
		mListadapter = createListAdapter(listItems);
		dialogBuilder.setSingleChoiceItems(mListadapter, -1, onClickListener);
		dialogBuilder.setCancelable(true);
		return dialogBuilder;
	}

    private View createTitlelayout() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View titleView = inflater.inflate(R.layout.view_file_dialog_title, null);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int dialogMargin = (int) mContext.getResources().getDimension(R.dimen.dialog_margin);
        int dialogBottomMargin = (int) mContext.getResources().getDimension(R.dimen.dialog_bottom_margin);
        titleParams.setMargins(dialogMargin, dialogMargin, dialogMargin, dialogBottomMargin);
        titleView.setLayoutParams(titleParams);

        TextView tvTitle = (TextView) titleView.findViewById(R.id.tvFileTitle);
        ImageButton addFolder = (ImageButton) titleView.findViewById(R.id.btnAddFolder);
        final String newFolder = mContext.getResources().getString(R.string.caption_new_folder_name);
        final String failedToCreateFolder = mContext.getResources().getString(R.string.message_failed_to_create_folder);
        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.editStringDialog(mContext,
                        newFolder,
                        "",
                        new DialogUtils.StringValueListener() {
                            @Override
                            public void onStringValue(String value) {
                                // Create new directory
                                if (createSubDir(mDir + "/" + value)) {
                                    mDir += "/" + value;
                                    updateDirectory();
                                } else {
                                    GuiUtils.showMessage(mContext, failedToCreateFolder.concat(" '").concat(value).concat("'"));
                                }
                            }
                        });
            }
        });
        addFolder.setVisibility((selectType == folderChoose || selectType == fileSave) ? View.VISIBLE : View.GONE);
        String selectFileCaption = mContext.getResources().getString(R.string.title_select_file);
        String folderSelectCaption = mContext.getResources().getString(R.string.caption_folder_select);

        switch (selectType) {
            case fileOpen:
                tvTitle.setText(selectFileCaption);
                break;
            case fileSave:
                tvTitle.setText(folderSelectCaption);
                break;
            case folderChoose:
                tvTitle.setText(folderSelectCaption);
                break;
            default:
                tvTitle.setText(selectFileCaption);
                break;
        }

        return titleView;
    }

    private void getBreadCrumb() {
        if (mCanExplore) {
            String[] dirs = mDir.split("/");
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
        }
        else {
            scrollView.setVisibility(View.GONE);
        }
    }

	private void updateDirectory() {
        mSubdirs.clear();
		String fileName = "";
        if (mDir == null || mDir.isEmpty()) {
            showRootDir();
        }
        else {
            mSubdirs.addAll(getDirectories(mDir));
        }
		// #scorch
		if (selectType == fileSave || selectType == fileOpen) {
			fileName = selectedFileName;
		}
        File file = new File(mDir + "/" + fileName);
        if (!file.isFile()) {
            getBreadCrumb();
        }
		mListadapter.notifyDataSetChanged();
	}

    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
    }

	private ArrayAdapter<RowItem> createListAdapter(List<RowItem> items) {
		return new ArrayAdapter<RowItem>(mContext,
				R.layout.view_file_dialog_item, items) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                RowItem rowItem = getItem(position);
                RelativeLayout rlDirItem;

                LayoutInflater mInflater = (LayoutInflater) mContext
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.view_file_dialog_item, null);
                    holder = new ViewHolder();
                    holder.txtTitle = (TextView) convertView.findViewById(R.id.tvFileItem);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.ivFileImage);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                rlDirItem = (RelativeLayout) convertView.findViewById(R.id.rlDirItem);
                holder.txtTitle.setText(rowItem.getTitle());
                int bottomToolbarHeight = (int) mContext.getResources().getDimension(R.dimen.bottom_toolbar_height);
                int dialogMargin = (int) mContext.getResources().getDimension(R.dimen.dialog_margin);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        bottomToolbarHeight);
                layoutParams.setMargins(dialogMargin, 0, 0, 0);
                holder.txtTitle.setLayoutParams(layoutParams);
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                if (rowItem.getImageId() != UNDEFINED_VALUE) {
                    holder.imageView.setImageResource(rowItem.getImageId());
                }
                else {
                    holder.imageView.setImageBitmap(null);
                }
                if (rowItem.getTitle().equals(selectedFileName)) {
                    rlDirItem.setBackgroundColor(mContext.getResources().getColor(R.color.file_dialog_color));
                    holder.txtTitle.setTextColor(Color.WHITE);
                }
                else {
                    rlDirItem.setBackgroundColor(mContext.getResources().getColor(android.R.color.background_light));
                    holder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.secondary_text));
                }

                return convertView;
			}

		};
	}
}
