package com.example.footfitstore.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<String> categoryList;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public CategoryAdapter(Context context, List<String> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.categoryTextView.setText(category);

        // Change background and text color based on selection
        if (position == selectedPosition) {
            holder.categoryTextView.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_background));
            holder.categoryTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white)); // Set text color to white
        } else {
            holder.categoryTextView.setBackground(ContextCompat.getDrawable(context, R.drawable.unselected_background));
            holder.categoryTextView.setTextColor(ContextCompat.getColor(context, R.color.black2B2B2B)); // Default text color
        }

        // Handle item clicks
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }
    }
}

