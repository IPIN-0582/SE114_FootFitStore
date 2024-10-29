package com.example.footfitstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.ProductDetailActivity;
import com.example.footfitstore.model.Shoe;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchShoeAdapter extends RecyclerView.Adapter<SearchShoeAdapter.SearchShoeViewHolder> {

    private List<Shoe> shoeList;
    private Context context;

    public SearchShoeAdapter(List<Shoe> shoeList, Context context) {
        this.shoeList = shoeList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchShoeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchShoeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchShoeViewHolder holder, int position) {
        Shoe shoe = shoeList.get(position);

        // Gán dữ liệu vào các View
        holder.tvShoeName.setText(shoe.getTitle());
        holder.tvShoePrice.setText("$" + shoe.getPrice());

        // Tải ảnh sản phẩm từ Firebase hoặc URL
        Picasso.get().load(shoe.getPicUrl().get(0)).into(holder.ivShoeImage);

        holder.itemView.setOnClickListener(v -> {
            // Tạo Intent để điều hướng đến ProductDetailActivity
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", shoe.getProductId()); // Truyền productId vào Intent
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return shoeList.size();
    }

    public static class SearchShoeViewHolder extends RecyclerView.ViewHolder {

        TextView tvShoeName, tvShoePrice;
        ImageView ivShoeImage;

        public SearchShoeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShoeName = itemView.findViewById(R.id.tvShoeName);
            tvShoePrice = itemView.findViewById(R.id.tvShoePrice);
            ivShoeImage = itemView.findViewById(R.id.ivShoeImage);
        }
    }
}