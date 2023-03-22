package com.tiromansev.filedialog.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tiromansev.filedialog.FileDialog;
import com.tiromansev.filedialog.IFileDialog;
import com.tiromansev.filedialog.SafDialog;
import com.tiromansev.filedialog.utils.GuiUtils;

public class MainActivity extends AppCompatActivity {

    private IFileDialog fileDialog;

    protected final ActivityResultLauncher<Intent> fileDialogLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (fileDialog != null) {
                    fileDialog.handleSafLauncherResult(result.getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenDialog = findViewById(R.id.btnOpenDialog);
        /*btnOpenDialog.setOnClickListener(v -> {
            fileDialog = FileDialog.create(MainActivity.this)
                    .setSelectType(FileDialog.FILE_SAVE)
                    .setFileName("new_file_name")
                    .setFileExt(".txt")
                    .setAddModifiedDate(true)
                    .setSafLauncher(fileDialogLauncher)
                    .setFileDialogListener((uri) ->
                            GuiUtils.showMessage(MainActivity.this, uri.toString()))
                    .build();
            fileDialog.show();
        });*/

        btnOpenDialog.setOnClickListener(v -> {
            fileDialog = SafDialog.create(MainActivity.this)
                    .setSelectType(FileDialog.FILE_SAVE)
                    .setMimeType("text/plain")
                    .setFileName("1234567")
                    //.setMimeType("application/vnd.ms-excel")
                    .setSafLauncher(fileDialogLauncher)
                    .setFileDialogListener((uri) ->
                            GuiUtils.showMessage(MainActivity.this, uri.toString()))
                    .build();
            fileDialog.show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (fileDialog != null) {
//            fileDialog.handleRequestResult(requestCode, resultCode, data);
//        }
    }
}
