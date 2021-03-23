package com.tiromansev.filedialog;

import androidx.documentfile.provider.DocumentFile;

import com.tiromansev.filedialog.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Single;

public class FileManager {

    private FileDialog fileDialog;

    public FileManager(FileDialog fileDialog) {
        this.fileDialog = fileDialog;
    }

    public Single<List<RowItem>> getFilesAsync(SafFile safFile) {
        return Single.create(emitter -> {
            List<RowItem> rowItems = new ArrayList<>();

            try {
                rowItems = getFiles(safFile);
            } catch (Exception e) {
                e.printStackTrace();
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                    return;
                }
            }

            if (!emitter.isDisposed()) {
                emitter.onSuccess(rowItems);
            }
        });
    }

    private List<RowItem> getFiles(SafFile safFile) {
        List<RowItem> files = new ArrayList<>();

        for (DocumentFile file : safFile.getFile().listFiles()) {
            if (file.isFile()) {
                boolean exclude = false;
                if (fileDialog.getFilterFileExt() != null && fileDialog.getFilterFileExt().length > 0) {
                    exclude = true;
                    for (String filter : fileDialog.getFilterFileExt()) {
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
                if (fileDialog.isAddModifiedDate()) {
                    data = FileUtils.size(file.length());
                    Date lastModDate = new Date(file.lastModified());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.ENGLISH);
                    data += dateFormat.format(lastModDate);
                }
                if (fileDialog.getFileIcons().size() > 0) {
                    for (Map.Entry<String, Integer> entry : fileDialog.getFileIcons().entrySet()) {
                        String name = file.getName();
                        if (name != null && name.endsWith(entry.getKey())) {
                            item = new RowItem(entry.getValue(), file.getName(), data, file.lastModified(), file.getUri());
                            files.add(item);
                            break;
                        }
                    }
                }
                if (item == null) {
                    item = new RowItem(fileDialog.getFileImageId(), file.getName(), data, file.lastModified(), file.getUri());
                    files.add(item);
                }
            }
        }

        Collections.sort(files, fileDialog.getFileComparator());
        return new ArrayList<>(files);
    }

}
