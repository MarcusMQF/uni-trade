package com.example.unitrade;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfirmDialog {

    public interface Listener {
        void onConfirm();
    }

    public static void show(Context context, String title, String message,
                            String confirmButtonText,
                            Listener listener) {

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true);

        TextView txtTitle = dialog.findViewById(R.id.txtTitle);
        TextView txtMessage = dialog.findViewById(R.id.txtMessage);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);   // CONFIRM BUTTON
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        ImageView btnClose = dialog.findViewById(R.id.btnClose);

        txtTitle.setText(title);
        txtMessage.setText(message);
        btnConfirm.setText(confirmButtonText);   // ← CHANGE BUTTON LABEL HERE

        btnConfirm.setOnClickListener(v -> {
            listener.onConfirm();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}