package com.example.footfitstore.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.CartRating;
import com.example.footfitstore.model.Promotion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("Shoes")
                .child(cartRating.getProductId())
                .child("promotion");
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Promotion promotion = dataSnapshot.getValue(Promotion.class);
                    if (promotion != null && isPromotionActive(promotion)) {
                        // Áp dụng giá khuyến mãi
                        double discount = promotion.getDiscount();
                        double discountedPrice = cartRating.getPrice() * (1 - discount / 100);

                        // Hiển thị giá gốc với gạch ngang và giá khuyến mãi
                        holder.tvProductOriginalPrice.setText("$" + String.format("%.2f", cartRating.getPrice()));
                        holder.tvProductOriginalPrice.setPaintFlags(holder.tvProductOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        holder.tvProductOriginalPrice.setVisibility(View.VISIBLE);

                        holder.tvProductPrice.setText("$" + String.format("%.2f", discountedPrice));
                    } else {
                        // Nếu không có khuyến mãi, chỉ hiển thị giá gốc
                        holder.tvProductOriginalPrice.setVisibility(View.GONE);
                        holder.tvProductPrice.setText("$" + String.format("%.2f", cartRating.getPrice()));
                    }
                } else {
                    // Nếu không có khuyến mãi, chỉ hiển thị giá gốc
                    holder.tvProductOriginalPrice.setVisibility(View.GONE);
                    holder.tvProductPrice.setText("$" + String.format("%.2f", cartRating.getPrice()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.tvProductPrice.setText("$" + cartRating.getPrice());
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
        TextView tvProductName, tvProductOriginalPrice, tvProductQuantity, tvProductSize, tvProductPrice;
        ImageView ivProductImage;
        RatingBar ratingBar;
        public CartRatingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName=itemView.findViewById(R.id.txt_name);
            tvProductOriginalPrice =itemView.findViewById(R.id.item_original_price);
            tvProductPrice = itemView.findViewById(R.id.txt_price);
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
    private boolean isPromotionActive(Promotion promotion) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(promotion.getStartDate());
            Date endDate = sdf.parse(promotion.getEndDate());
            Date today = new Date();
            today = resetTime(today);
            return (today.after(startDate)||today.equals(startDate)) && (today.before(endDate) || today.equals(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static Date resetTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
