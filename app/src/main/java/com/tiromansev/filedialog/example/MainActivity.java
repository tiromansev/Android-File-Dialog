package com.tiromansev.filedialog.example;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.anggrayudi.storage.SimpleStorageHelper;
import com.tiromansev.filedialog.FileDialog;
import com.tiromansev.filedialog.IFileDialog;
import com.tiromansev.filedialog.SafDialog;
import com.tiromansev.filedialog.utils.GuiUtils;

public class MainActivity extends AppCompatActivity {

    private IFileDialog fileDialog;
    private SimpleStorageHelper storageHelper = new SimpleStorageHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenDialog = findViewById(R.id.btnOpenDialog);

        btnOpenDialog.setOnClickListener(v -> {
            fileDialog = SafDialog.create(MainActivity.this, storageHelper)
                    .setSelectType(FileDialog.FOLDER_CHOOSE)
                    .setMimeTypes(SafDialog.EXCEL_FILE_MIMES)
                    .setFileDialogListener((uri) -> {
                        GuiUtils.showMessage(MainActivity.this, uri.toString());
                        Log.d("selected_file", "uri: " + uri.toString());
                    })
                    .build();
            fileDialog.show();
        });
    }
}
