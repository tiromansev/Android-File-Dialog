package com.tiromansev.filedialog.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tiromansev.filedialog.FileDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpenDialog = (Button) findViewById(R.id.btnOpenDialog);
        btnOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileDialog.create(MainActivity.this)
                        .setCanExplore(true)
                        .setSelectType(FileDialog.FILE_OPEN)
                        .setAddModifiedDate(true)
                        .setFileDialogListener(new FileDialog.FileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                Toast.makeText(MainActivity.this, chosenDir, Toast.LENGTH_LONG).show();
                            }
                        })
                        .build()
                        .show(getExternalFilesDir(null).getAbsolutePath());
            }
        });
    }
}
