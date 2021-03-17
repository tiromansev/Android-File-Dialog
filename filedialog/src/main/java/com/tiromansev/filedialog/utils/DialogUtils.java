package com.tiromansev.filedialog.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.tiromansev.filedialog.R;

public class DialogUtils {

    public static void editStringDialog(final Activity context,
                                        String title,
                                        String value,
                                        final StringValueListener valueListener) {
        LinearLayout dialogView =
                (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_string, null);
        final EditText edtValue = dialogView.findViewById(R.id.edtStringValue);
        edtValue.setText(value);
        new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setCancelable(true)
                .setView(dialogView)
                .setPositiveButton(
                        R.string.caption_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (edtValue.getText().toString().isEmpty()) {
                                    GuiUtils.showMessage(context, R.string.message_value_cannot_be_empty);
                                }
                                else {
                                    valueListener.onStringValue(edtValue.getText().toString());
                                }
                            }
                        })
                .show();
        GuiUtils.toggleSoftKeyboard(context);
    }

    public static void showQuestionDialog(Activity context,
                                          String message,
                                          DialogInterface.OnClickListener okListener,
                                          DialogInterface.OnClickListener cancelListener) {
        AlertDialog.Builder dialog =
                new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        dialog.setTitle(null);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setPositiveButton(R.string.caption_use_dir, okListener);
        dialog.setNegativeButton(R.string.caption_select_other, cancelListener);
        if (!context.isFinishing()) {
            dialog.show();
        }
    }

    public interface StringValueListener {
        void onStringValue(String value);
    }

}
