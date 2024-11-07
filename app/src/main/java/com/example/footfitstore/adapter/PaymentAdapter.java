package com.example.footfitstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.footfitstore.R;
import com.example.footfitstore.model.PaymentMethod;

import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends ArrayAdapter<PaymentMethod> {
    private Context context;
    private List<PaymentMethod> paymentMethodList;

    public PaymentAdapter( int resource, Context context1, List<PaymentMethod> paymentMethodList) {
        super(context1, resource, paymentMethodList);
        this.context = context1;
        this.paymentMethodList = paymentMethodList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_payment_method,parent,false);
        TextView t1=convertView.findViewById(R.id.txt_paymentName);
        ImageView i1= convertView.findViewById(R.id.img_payment);
        PaymentMethod paymentMethod = this.getItem(position);
        if (paymentMethod != null)
        {
            t1.setText(paymentMethod.getName());
            i1.setImageResource(paymentMethod.getImage());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_payment_method,parent,false);
        TextView t1=convertView.findViewById(R.id.txt_paymentName);
        ImageView i1= convertView.findViewById(R.id.img_payment);
        PaymentMethod paymentMethod = this.getItem(position);
        if (paymentMethod != null)
        {
            t1.setText(paymentMethod.getName());
            i1.setImageResource(paymentMethod.getImage());
        }
        return convertView;
    }
}
