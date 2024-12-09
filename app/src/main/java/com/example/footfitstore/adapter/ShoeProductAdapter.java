package com.example.footfitstore.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.Shoe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ShoeProductAdapter extends RecyclerView.Adapter<ShoeProductAdapter.ShoeProductViewHolder> {
    private Context context;
    private List<Shoe> allShoeList;

    public ShoeProductAdapter(Context context, List<Shoe> allShoeList) {
        this.context = context;
        this.allShoeList = allShoeList;
    }

    @NonNull
    @Override
    public ShoeProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shoe_product,null);
        return new ShoeProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoeProductViewHolder holder, int position) {
        Shoe shoe = allShoeList.get(position);
        holder.titleTextView.setText(shoe.getTitle());
        holder.priceTextView.setText("$ "+ shoe.getPrice());
        if (shoe.getPicUrl() != null && !shoe.getPicUrl().isEmpty()) {
            Picasso.get().load(shoe.getPicUrl().get(0)).into(holder.itemImageView);
        }
    }

    @Override
    public int getItemCount() {
        return allShoeList.size();
    }

    public static class ShoeProductViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView titleTextView, priceTextView;
        public ShoeProductViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImage);
            titleTextView = itemView.findViewById(R.id.itemTitle);
            priceTextView = itemView.findViewById(R.id.itemPrice);
        }
    }
}
