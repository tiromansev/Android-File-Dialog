package com.tiromansev.filedialog.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tiromansev.filedialog.FileDialog;
import com.tiromansev.filedialog.utils.GuiUtils;

public class MainActivity extends AppCompatActivity {

    private FileDialog fileDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenDialog = findViewById(R.id.btnOpenDialog);
        btnOpenDialog.setOnClickListener(v -> {
            fileDialog = FileDialog.create(MainActivity.this)
                    .setSelectType(FileDialog.FILE_SAVE)
                    .setFileName("new_file_name")
                    .setFileExt(".txt")
                    .setAddModifiedDate(true)
                    .setFileDialogListener((uri, fileName) ->
                            GuiUtils.showMessage(MainActivity.this, uri.toString()))
                    .build();
            fileDialog.show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fileDialog != null) {
            fileDialog.handleRequestResult(requestCode, resultCode, data);
        }
    }
}
