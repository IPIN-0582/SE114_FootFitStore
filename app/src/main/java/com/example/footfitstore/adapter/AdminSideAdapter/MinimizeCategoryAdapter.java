package com.example.footfitstore.adapter.AdminSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.footfitstore.R;

import java.util.List;

public class MinimizeCategoryAdapter extends ArrayAdapter<String> {
    private final List<String> categoryList;

    public MinimizeCategoryAdapter(@NonNull Context context, int resource, List<String> categoryList) {
        super(context, resource, categoryList);
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_picked,parent,false);
        TextView t1=convertView.findViewById(R.id.category);
        String category = categoryList.get(position);
        if (category != null)
        {
            t1.setText(category);
        }
        return convertView;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_spinner,parent,false);
        TextView t1=convertView.findViewById(R.id.category);
        String category = categoryList.get(position);
        if (category != null)
        {
            t1.setText(category);
        }
        return convertView;
    }
}
