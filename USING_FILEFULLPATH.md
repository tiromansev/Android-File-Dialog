# Using FileFullPath with SafDialog

This guide explains how to use `FileFullPath` to pre-select initial directories when opening file/folder pickers in SafDialog.

## What Was Done

### 1. Kotlin Version Upgrade
Updated Kotlin from version 1.9.25 to 2.2.0 to match the SimpleStorage library (v2.2.0) requirements.

**File**: `build.gradle:4`
```gradle
ext.kotlin_version = '2.2.0'
```

### 2. Folder URI Memory Feature
SafDialog now automatically remembers the last selected folder across app sessions:

- **AppPrefs.java**: Added `lastSafFolderUri()` preference method
- **SafDialog.java**: Added automatic folder URI saving and retrieval methods

**New Methods**:
```java
// Get the last selected folder URI
Uri lastFolder = SafDialog.getLastFolderUri();

// Clear the saved folder URI
SafDialog.clearLastFolderUri();
```

## Using FileFullPath (Advanced)

### Current Limitation

The current implementation uses `SimpleStorageHelper` basic methods without `initialPath` parameter. While SimpleStorage 2.2.0 supports `FileFullPath`, using it from Java requires additional setup.

### Option 1: Using Kotlin Extension (Recommended)

If you want to use `FileFullPath` to pre-select folders, create a Kotlin helper class:

**File**: `SafDialogHelper.kt`
```kotlin
package com.tiromansev.filedialog

import android.content.Context
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.StorageId

object SafDialogHelper {

    @JvmStatic
    fun openFilePickerWithInitialPath(
        storageHelper: SimpleStorageHelper,
        context: Context,
        requestCode: Int,
        allowMultiple: Boolean,
        mimeType: String,
        folderPath: String
    ) {
        val initialPath = FileFullPath(context, StorageId.PRIMARY, folderPath)
        storageHelper.openFilePicker(
            requestCode = requestCode,
            allowMultiple = allowMultiple,
            filterMimeTypes = arrayOf(mimeType),
            initialPath = initialPath
        )
    }

    @JvmStatic
    fun createFileWithInitialPath(
        storageHelper: SimpleStorageHelper,
        context: Context,
        mimeType: String,
        fileName: String,
        folderPath: String
    ) {
        val initialPath = FileFullPath(context, StorageId.PRIMARY, folderPath)
        storageHelper.createFile(
            mimeType = mimeType,
            fileName = fileName,
            initialPath = initialPath
        )
    }

    @JvmStatic
    fun openFolderPickerWithInitialPath(
        storageHelper: SimpleStorageHelper,
        context: Context,
        requestCode: Int,
        folderPath: String
    ) {
        val initialPath = FileFullPath(context, StorageId.PRIMARY, folderPath)
        storageHelper.openFolderPicker(requestCode, initialPath)
    }
}
```

Then use it from Java:
```java
// In your Activity
SafDialogHelper.createFileWithInitialPath(
    getStorageHelper(),
    this,
    "application/octet-stream",
    "document.xlsx",
    "Documents/MyApp"
);
```

### Option 2: Extract Path from Saved URI

Use the saved URI to determine the folder path:

```java
Uri lastFolder = SafDialog.getLastFolderUri();
if (lastFolder != null) {
    // Get DocumentFile from URI
    DocumentFile folder = DocumentFile.fromTreeUri(context, lastFolder);
    if (folder != null) {
        String folderName = folder.getName();
        // Use folder information as needed
    }
}
```

### Option 3: Modify SafDialog Directly

You can modify `SafDialog.launchSaf()` to use FileFullPath by creating Kotlin methods or using reflection (not recommended).

## How Folder Remembering Works

1. **Automatic Saving**: When you select a file/folder, SafDialog automatically saves the folder URI
2. **Persistence**: The URI is stored in SharedPreferences via `AppPrefs.lastSafFolderUri()`
3. **Extraction Logic**:
   - For `FILE_SAVE`: Extracts parent folder from created file URI
   - For `FILE_OPEN`: Extracts parent folder from selected file URI
   - For `FOLDER_CHOOSE`: Saves folder URI directly

## Example Usage

### Basic Usage (Already Working)
```java
fileDialog = SafDialog.create(getBaseActivity(), getBaseActivity().getStorageHelper())
    .setSelectType(IFileDialog.FILE_SAVE)
    .setFileName("document.xlsx")
    .setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    .setFileNameDialogListener((uri, s) -> {
        // Folder URI is automatically saved here
        // Next time dialog opens, you can access it via:
        // Uri lastFolder = SafDialog.getLastFolderUri();

        // Your file handling code
        documentExportPresenter.saveDocumentToExcel(...);
    })
    .build();
fileDialog.show();
```

### Getting Last Folder Info
```java
Uri lastFolder = SafDialog.getLastFolderUri();
if (lastFolder != null) {
    Log.d("SafDialog", "Last folder: " + lastFolder.toString());

    // Optional: Display to user or use for other purposes
    SafFile safFile = new SafFile(context, lastFolder);
    String folderName = safFile.getName();
}
```

### Clearing Saved Folder
```java
// Clear if user wants to reset or on logout
SafDialog.clearLastFolderUri();
```

## Storage IDs

When using FileFullPath, you can specify different storage locations:

- `StorageId.PRIMARY` - Internal storage
- `StorageId.EXTERNAL` - SD card (if available)
- Custom storage ID from `DocumentFile`

## Path Examples

Valid paths for `FileFullPath`:
- `"Documents"` - Documents folder
- `"Download"` - Downloads folder
- `"Documents/MyApp"` - Custom subfolder
- `"DCIM/Camera"` - Camera photos

## Troubleshooting

### "Module was compiled with incompatible Kotlin version"
- **Solution**: Already fixed by upgrading to Kotlin 2.2.0

### FileFullPath class not found in Java
- **Solution**: Use Option 1 (Kotlin helper) or implement custom ActivityResultContracts

### Initial folder not working
- **Limitation**: Android's document picker may ignore initial path in some cases
- The saved URI feature will still work for tracking user's last location

## Summary

✅ **Implemented**: Automatic folder URI memory in SafDialog
✅ **Available**: Static methods to get/clear last folder URI
✅ **Compatible**: Kotlin 2.2.0 + SimpleStorage 2.2.0
⚠️ **Advanced**: FileFullPath requires Kotlin helper for Java projects

For most use cases, the automatic folder memory feature should be sufficient!
