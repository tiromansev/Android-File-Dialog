package com.tiromansev.filedialog;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;

public class DialogUtils {

    public static void editStringDialog(final Activity context,
                                        String title,
                                        String value,
                                        final StringValueListener valueListener) {
        LinearLayout dialogView =
                (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_string, null);
        final EditText edtValue = (EditText) dialogView.findViewById(R.id.edtStringValue);
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

    public interface StringValueListener {
        void onStringValue(String value);
    }

}
