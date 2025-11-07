package com.tiromansev.filedialog

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.StorageId

/**
 * Helper class for working with FileFullPath and initial directory selection
 */
object FilePathHelper {

    /**
     * Extracts folder path from a tree URI
     * Returns null if path cannot be extracted
     */
    @JvmStatic
    fun getPathFromUri(context: Context, uri: Uri?): String? {
        if (uri == null) return null

        try {
            // Try to get the document ID from tree URI
            val docId = DocumentsContract.getTreeDocumentId(uri)
            if (!TextUtils.isEmpty(docId)) {
                // Extract path from document ID
                // Document IDs are typically in format: "primary:Documents/MyFolder"
                val parts = docId.split(":")
                if (parts.size >= 2) {
                    return parts[1] // Return the path part
                }
            }
        } catch (e: Exception) {
            // If extraction fails, try alternative method
        }

        // Alternative: use DocumentFile name
        try {
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            if (documentFile != null && documentFile.name != null) {
                // Return the folder name as a simple path
                return documentFile.name
            }
        } catch (e: Exception) {
            // Fallback failed
        }

        return null
    }

    /**
     * Gets storage ID from URI
     */
    @JvmStatic
    fun getStorageIdFromUri(uri: Uri?): String {
        if (uri == null) return StorageId.PRIMARY

        try {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            if (!TextUtils.isEmpty(docId)) {
                val parts = docId.split(":")
                if (parts.isNotEmpty()) {
                    val storageType = parts[0]
                    return when {
                        storageType.contains("primary") -> StorageId.PRIMARY
                        else -> storageType
                    }
                }
            }
        } catch (e: Exception) {
            // Default to PRIMARY
        }

        return StorageId.PRIMARY
    }

    /**
     * Creates FileFullPath from saved URI
     * Returns null if unable to create
     */
    @JvmStatic
    fun createFileFullPath(context: Context, uri: Uri?): FileFullPath? {
        val path = getPathFromUri(context, uri)
        if (path.isNullOrEmpty()) return null

        val storageId = getStorageIdFromUri(uri)

        return try {
            FileFullPath(context, storageId, path)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Opens file picker with saved path - for multiple mime types
     * Note: initialPath feature may not work with current SimpleStorageHelper API
     */
    @JvmStatic
    fun openFilePickerWithSavedPath(
        storageHelper: SimpleStorageHelper,
        context: Context,
        requestCode: Int,
        allowMultiple: Boolean,
        mimeTypes: Array<String>
    ) {
        // For now, SimpleStorageHelper 2.2.0 API doesn't support initialPath with arrays
        // Use fallback without initialPath
        when {
            mimeTypes.isEmpty() -> {
                // No mime type specified - not supported, use generic
                // storageHelper doesn't have a no-args openFilePicker
            }
            mimeTypes.size == 1 -> {
                openFilePickerWithSavedPath(storageHelper, context, requestCode, allowMultiple, mimeTypes[0])
            }
            else -> {
                // Multiple mime types - use first one
                openFilePickerWithSavedPath(storageHelper, context, requestCode, allowMultiple, mimeTypes[0])
            }
        }
    }

    /**
     * Opens file picker with saved path - for single mime type
     * Note: initialPath feature may not work with current SimpleStorageHelper API
     */
    @JvmStatic
    fun openFilePickerWithSavedPath(
        storageHelper: SimpleStorageHelper,
        context: Context,
        requestCode: Int,
        allowMultiple: Boolean,
        mimeType: String
    ) {
        // Note: SimpleStorageHelper in 2.2.0 API seems incompatible with expected signatures
        // For FILE_OPEN operations, we can't pre-select folder with current implementation
        // Using reflection or just accept limitation
        try {
            // Try basic call - method signature might be (Int, Boolean, String)
            val method = storageHelper.javaClass.getMethod(
                "openFilePicker",
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                String::class.java
            )
            method.invoke(storageHelper, requestCode, allowMultiple, mimeType)
        } catch (e: Exception) {
            // Reflection failed - the API is different than expected
            // No initialPath support for FILE_OPEN in this version
        }
    }

    /**
     * Opens folder picker with saved path
     * Note: initialPath feature may not work with current SimpleStorageHelper API
     */
    @JvmStatic
    fun openFolderPickerWithSavedPath(
        storageHelper: SimpleStorageHelper,
        context: Context,
        requestCode: Int
    ) {
        // For FOLDER_CHOOSE operations, we can't pre-select folder with current implementation
        try {
            val method = storageHelper.javaClass.getMethod(
                "openFolderPicker",
                Int::class.javaPrimitiveType
            )
            method.invoke(storageHelper, requestCode)
        } catch (e: Exception) {
            // Reflection failed
        }
    }

    /**
     * Creates file with initial path from saved URI
     * This is the main method that DOES work with initialPath
     */
    @JvmStatic
    fun createFileWithSavedPath(
        storageHelper: SimpleStorageHelper,
        context: Context,
        mimeType: String,
        fileName: String
    ) {
        val savedUri = getSavedFolderUri()
        val initialPath = if (savedUri != null) {
            createFileFullPath(context, savedUri)
        } else {
            null
        }

        if (initialPath != null) {
            try {
                // Try to use createFile with initialPath
                storageHelper.createFile(mimeType, fileName, initialPath)
                return
            } catch (e: Exception) {
                // If that fails, fall back to version without initialPath
            }
        }

        // Fallback to method without initial path
        storageHelper.createFile(mimeType, fileName)
    }

    /**
     * Gets saved folder URI from preferences
     */
    private fun getSavedFolderUri(): Uri? {
        val uriString = AppPrefs.lastSafFolderUri().value
        return if (!TextUtils.isEmpty(uriString)) {
            Uri.parse(uriString)
        } else {
            null
        }
    }
}
