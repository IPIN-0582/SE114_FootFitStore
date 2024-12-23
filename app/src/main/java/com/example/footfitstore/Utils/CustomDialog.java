package com.example.footfitstore.Utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.footfitstore.R;

public class CustomDialog {
    private Dialog dialog;
    private TextView titleView, messageView;
    private ImageView iconView;
    private Button btnOk, btnCancel;
    private final Context context;

    public interface OnDialogClickListener {
        void onPositiveClick();
        void onNegativeClick();
    }

    public CustomDialog(Context context) {
        this.context = context;
        setupDialog();
    }

    private void setupDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(window.getAttributes());
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(params);
        }

        titleView = dialog.findViewById(R.id.dialogTitle);
        messageView = dialog.findViewById(R.id.dialogMessage);
        iconView = dialog.findViewById(R.id.dialogIcon);
        btnOk = dialog.findViewById(R.id.btnOk);
        btnCancel = dialog.findViewById(R.id.btnCancel);
    }


    public CustomDialog setTitle(String title) {
        titleView.setText(title);
        return this;
    }

    public CustomDialog setMessage(String message) {
        messageView.setText(message);
        return this;
    }

    public CustomDialog setIcon(int iconResource) {
        iconView.setImageResource(iconResource);
        iconView.setVisibility(View.VISIBLE);
        return this;
    }

    public CustomDialog hideIcon() {
        iconView.setVisibility(View.GONE);
        return this;
    }

    public CustomDialog setPositiveButton(String text, final OnDialogClickListener listener) {
        btnOk.setText(text);
        btnOk.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPositiveClick();
            }
            dialog.dismiss();
        });
        return this;
    }

    public CustomDialog setNegativeButton(String text, final OnDialogClickListener listener) {
        btnCancel.setText(text);
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNegativeClick();
            }
            dialog.dismiss();
        });
        return this;
    }

    public CustomDialog hideNegativeButton() {
        btnCancel.setVisibility(View.GONE);
        return this;
    }

    public CustomDialog setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
