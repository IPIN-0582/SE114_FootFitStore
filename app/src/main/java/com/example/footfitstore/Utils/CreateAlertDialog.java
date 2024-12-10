package com.example.footfitstore.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.footfitstore.R;

public class CreateAlertDialog {
    private final Context context;

    public CreateAlertDialog(Context context) {
        this.context = context;
    }
    @SuppressLint("InflateParams")
    public void createDialog(String s)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        final View customLayout;
        customLayout = LayoutInflater.from(context).inflate(R.layout.dialog_payment_cancelled, null);
        builder.setView(customLayout);
        TextView message = customLayout.findViewById(R.id.txt_success);
        Button positiveButton = customLayout.findViewById(R.id.pos_button);
        message.setText(s);
        AlertDialog alertDialog = builder.create();
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
