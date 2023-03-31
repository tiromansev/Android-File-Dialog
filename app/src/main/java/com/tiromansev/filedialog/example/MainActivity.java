package com.tiromansev.filedialog.example;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

        btnOpenDialog.setOnClickListener(v -> {
            fileDialog = SafDialog.create(MainActivity.this)
                    .setSelectType(FileDialog.FILE_OPEN)
                    .setMimeTypes(SafDialog.EXCEL_FILE_MIMES)
                    .setFileName("1234567")
                    .setSafLauncher(fileDialogLauncher)
                    .setFileNameDialogListener((uri, s) -> {
                        GuiUtils.showMessage(MainActivity.this, uri.toString());
                        Log.d("selected_file", "uri: " + uri.toString());
                    })
                    .build();
            fileDialog.show();
        });
    }
}
