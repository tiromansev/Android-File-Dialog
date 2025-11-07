# Updating Your Project to Use the New Features

This guide explains what to do in your project that uses the Android-File-Dialog library.

## Option 1: Use as Local Library (Immediate)

If you're using this library as a local module in your project:

### Step 1: Sync Your Code
Pull the latest changes from this repository into your project.

### Step 2: Update Your Project's Kotlin Version
Update your project's root `build.gradle`:

```gradle
buildscript {
    ext.kotlin_version = '2.2.0'  // Update from 1.9.25 to 2.2.0
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.13.0'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
```

### Step 3: Update SimpleStorage Dependency (If Not Inherited)
If your app module has its own SimpleStorage dependency, update it:

```gradle
dependencies {
    implementation 'com.anggrayudi:storage:2.2.0'  // Make sure it's 2.2.0
}
```

### Step 4: Clean and Rebuild
```bash
./gradlew clean
./gradlew build
```

### Step 5: Test Your Code
Your existing SafDialog code should work exactly the same, but now it automatically remembers folder locations!

## Option 2: Publish and Use as Maven/JitPack Dependency

If you want to publish this library first:

### Step 1: Publish the Library
Publish to Maven Local for testing:
```bash
cd /path/to/Android-File-Dialog
./gradlew :filedialog:publishToMavenLocal
```

Or publish to JitPack by creating a GitHub release.

### Step 2: Update Your Project Dependencies
In your project's `build.gradle`:

```gradle
dependencies {
    // Update to the new version
    implementation 'com.github.tiromansev:Android-File-Dialog:NEW_VERSION'
}
```

## What Your Existing Code Gets Automatically

### ✅ Automatic Folder Memory
Your existing code already benefits from folder URI memory:

```java
fileDialog = SafDialog.create(getBaseActivity(), getBaseActivity().getStorageHelper())
    .setSelectType(IFileDialog.FILE_SAVE)
    .setFileName(document.getDocFileName() +
            StockApp.getPrefs().getExternalFileType().getFileExt())
    .setMimeType(StockApp.getPrefs().getExternalFileType().getFileMime())
    .setFileNameDialogListener((uri, s) -> {
        // ✅ The folder URI is automatically saved!
        documentExportPresenter.saveDocumentToExcel(...);
    })
    .build();
fileDialog.show();
```

**No code changes needed!** The library now:
- Saves the folder URI after each file selection
- Stores it in SharedPreferences
- Makes it available for next time

## Optional: Access the Saved Folder URI

If you want to use the saved folder URI in your code:

### Get Last Selected Folder
```java
// Anywhere in your code
Uri lastFolder = SafDialog.getLastFolderUri();
if (lastFolder != null) {
    Log.d("MyApp", "Last used folder: " + lastFolder.toString());

    // Get folder name
    SafFile safFile = new SafFile(this, lastFolder);
    String folderName = safFile.getName();

    // Display to user
    Toast.makeText(this, "Last folder: " + folderName, Toast.LENGTH_SHORT).show();
}
```

### Clear Saved Folder
```java
// Clear on user logout or when resetting app
SafDialog.clearLastFolderUri();
```

### Display Last Folder to User
```java
// Show user where files will be saved
private void showLastSaveLocation() {
    Uri lastFolder = SafDialog.getLastFolderUri();
    if (lastFolder != null) {
        SafFile safFile = new SafFile(this, lastFolder);
        String folderName = safFile.getName();
        tvLastLocation.setText("Last save location: " + folderName);
    } else {
        tvLastLocation.setText("No previous save location");
    }
}
```

## Advanced: Using FileFullPath for Initial Directory

If you want to pre-select a specific folder when opening the picker, you need to create a Kotlin helper.

### Step 1: Create Kotlin Helper File
In your project, create `SafDialogHelper.kt`:

```kotlin
package com.yourapp.helpers

import android.content.Context
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.StorageId

object SafDialogHelper {

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

### Step 2: Use in Your Activity
Instead of using SafDialog.create(), call SimpleStorageHelper directly:

```java
// Set up callbacks first (in onCreate or similar)
getStorageHelper().setOnFileCreated((requestCode, file) -> {
    // Handle file created
    documentExportPresenter.saveDocumentToExcel(..., file.getUri(), ...);
    return null;
});

// Then call with initial path
SafDialogHelper.createFileWithInitialPath(
    getStorageHelper(),
    this,
    StockApp.getPrefs().getExternalFileType().getFileMime(),
    document.getDocFileName() + StockApp.getPrefs().getExternalFileType().getFileExt(),
    "Documents/Exports"  // Initial folder path
);
```

## Common Scenarios

### Scenario 1: Just Want Automatic Memory (Easiest)
**Do nothing!** Your code already works and now has folder memory.

### Scenario 2: Want to Show User Last Location
```java
// In your UI code
private void updateLastLocationDisplay() {
    Uri lastFolder = SafDialog.getLastFolderUri();
    if (lastFolder != null) {
        SafFile safFile = new SafFile(this, lastFolder);
        tvLastFolder.setText("Saves to: " + safFile.getName());
    }
}
```

### Scenario 3: Want to Pre-select Specific Folders
Use the Kotlin helper approach (see Advanced section above).

### Scenario 4: Want to Reset on App Reset/Logout
```java
private void resetApp() {
    // Clear other app data...

    // Clear saved folder location
    SafDialog.clearLastFolderUri();
}
```

## Testing the Changes

### Test 1: Basic Functionality
1. Run your app
2. Open SafDialog and save a file to a folder
3. Close and reopen your app
4. The folder URI is now saved in SharedPreferences

### Test 2: Verify Folder Memory
```java
// Add logging to verify
Uri lastFolder = SafDialog.getLastFolderUri();
Log.d("TEST", "Saved folder: " + (lastFolder != null ? lastFolder.toString() : "none"));
```

### Test 3: Multiple Saves
1. Save a file to folder A
2. Check `SafDialog.getLastFolderUri()` - should be folder A
3. Save a file to folder B
4. Check `SafDialog.getLastFolderUri()` - should be folder B

## Troubleshooting

### Build Error: Kotlin Version Mismatch
**Problem**: `Module was compiled with an incompatible version of Kotlin`

**Solution**: Update your project's Kotlin version to 2.2.0 (see Step 2 above)

### SafDialog.getLastFolderUri() Returns Null
**Possible causes**:
1. User hasn't selected any folder yet (first run)
2. SharedPreferences was cleared
3. App was uninstalled and reinstalled

**Solution**: Handle null case in your code:
```java
Uri lastFolder = SafDialog.getLastFolderUri();
if (lastFolder == null) {
    // First time use or preferences cleared
    showMessage("Please select a folder");
}
```

### FileFullPath Not Found
**Problem**: Can't use FileFullPath in Java

**Solution**: Create the Kotlin helper (see Advanced section)

## Migration Checklist

- [ ] Update root `build.gradle` to Kotlin 2.2.0
- [ ] Update SimpleStorage to 2.2.0 (if specified in app module)
- [ ] Clean and rebuild project
- [ ] Test existing SafDialog code (should work unchanged)
- [ ] Optionally: Add code to display last folder to user
- [ ] Optionally: Create Kotlin helper for FileFullPath
- [ ] Test folder memory functionality

## Need Help?

Check these files in the library project:
- `USING_FILEFULLPATH.md` - Complete FileFullPath guide
- `CLAUDE.md` - Project architecture documentation
- `SafDialog.java` - Implementation details

## Summary

**Minimal Required Changes**: Just update Kotlin version to 2.2.0

**Your existing code gets**: Automatic folder memory for free!

**Optional enhancements**: Access saved folder URI or use FileFullPath for pre-selection
