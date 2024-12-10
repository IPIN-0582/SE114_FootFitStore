package com.example.footfitstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.footfitstore.R;
import com.example.footfitstore.model.ShoeMinimize;
import com.example.footfitstore.model.PaymentMethod;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MinimizeShoeAdapter extends ArrayAdapter<ShoeMinimize> {
    private final List<ShoeMinimize> minimizeShoeList;

    public MinimizeShoeAdapter(@NonNull Context context, int resource, List<ShoeMinimize> minimizeShoeList) {
        super(context, resource, minimizeShoeList);
        this.minimizeShoeList = minimizeShoeList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shoe_minimize_selected,parent,false);
        TextView t1=convertView.findViewById(R.id.txt_ShoeName);
        ImageView i1= convertView.findViewById(R.id.img_shoe);
        ShoeMinimize minimizeShoe = minimizeShoeList.get(position);
        if (minimizeShoe != null)
        {
            t1.setText(minimizeShoe.getShoeName());
            Picasso.get().load(minimizeShoe.getAvatarUrl()).into(i1);
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shoe_minimize,parent,false);
        TextView t1=convertView.findViewById(R.id.txt_shoe);
        ImageView i1= convertView.findViewById(R.id.img_shoe);
        ShoeMinimize minimizeShoe = minimizeShoeList.get(position);
        if (minimizeShoe != null)
        {
            t1.setText(minimizeShoe.getShoeName());
            Picasso.get().load(minimizeShoe.getAvatarUrl()).into(i1);
        }
        return convertView;
    }
}
