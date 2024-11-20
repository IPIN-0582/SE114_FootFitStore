package com.example.footfitstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.CartRating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CartRatingAdapter extends RecyclerView.Adapter<CartRatingAdapter.CartRatingViewHolder> {
    private Context context;
    private List<CartRating> cartRatings;

    public CartRatingAdapter(Context context, List<CartRating> cartRatings) {
        this.context = context;
        this.cartRatings = cartRatings;
    }

    @NonNull
    @Override
    public CartRatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_rating,parent,false);
        return new CartRatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartRatingViewHolder holder, int position) {
        CartRating cartRating = cartRatings.get(position);
        holder.tvProductName.setText(cartRating.getProductName());
        holder.tvProductSize.setText(cartRating.getSize());
        holder.tvProductQuantity.setText(String.valueOf(cartRating.getQuantity()));
        holder.tvProductPrice.setText("$" +cartRating.getPrice());
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                cartRating.setRating(rating);
            }
        });

        // Restore rating value if exists
        holder.ratingBar.setRating((float) cartRating.getRating());
        loadImageFromFirebase(cartRating.getProductId(), holder.ivProductImage);
    }

    @Override
    public int getItemCount() {
        return cartRatings.size();
    }


    static class CartRatingViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductSize;
        ImageView ivProductImage;
        RatingBar ratingBar;
        public CartRatingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName=itemView.findViewById(R.id.txt_name);
            tvProductPrice=itemView.findViewById(R.id.txt_price);
            tvProductQuantity=itemView.findViewById(R.id.txt_quantity);
            tvProductSize=itemView.findViewById(R.id.txt_size);
            ivProductImage=itemView.findViewById(R.id.imgView_image);
            ratingBar=itemView.findViewById(R.id.ratingBar);
        }
    }
    private void loadImageFromFirebase(String productId, ImageView imageView) {
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("Shoes")
                .child(productId)
                .child("picUrl")
                .child("0");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.getValue(String.class);
                    // Sử dụng Picasso hoặc Glide để tải ảnh
                    Picasso.get().load(imageUrl).into(imageView);  // Dùng Picasso
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
}
