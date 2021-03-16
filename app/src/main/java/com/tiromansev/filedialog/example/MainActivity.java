package com.tiromansev.filedialog.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tiromansev.filedialog.FileDialog;
import com.tiromansev.filedialog.FileDialogListener;

public class MainActivity extends AppCompatActivity {

    private FileDialog fileDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenDialog = findViewById(R.id.btnOpenDialog);
        btnOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileDialog = FileDialog.create(MainActivity.this)
                        .setCanExplore(true)
                        .setSelectType(FileDialog.FILE_OPEN)
                        .setAddModifiedDate(true)
                        .setFileDialogListener(new FileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                Toast.makeText(MainActivity.this, chosenDir, Toast.LENGTH_LONG).show();
                            }
                        })
                        .build();
                fileDialog.show(getExternalFilesDir(null).getAbsolutePath());
            }
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
