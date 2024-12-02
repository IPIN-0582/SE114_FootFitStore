package com.example.footfitstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.activity.ProductDetailActivity;
import com.example.footfitstore.fragment.FavouriteFragment;
import com.example.footfitstore.model.Promotion;
import com.example.footfitstore.model.Shoe;
import com.example.footfitstore.fragment.ExploreFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.internal.http2.Http2Connection;

public class ShoeAdapter extends RecyclerView.Adapter<ShoeAdapter.ItemViewHolder> {
    private List<Shoe> popularShoesList;  // Danh sách giày phổ biến
    private List<Shoe> allShoesList;      // Danh sách tất cả giày
    private Context context;
    private String viewType;  // Cờ để xác định loại dữ liệu (popular hay all)
    private ExploreFragment exploreFragment;
    private BottomSheetListener listener;
    private FavouriteFragment favouriteFragment;
    //Tạo interface cho Size Pick Listener
    public interface BottomSheetListener{
        void showBottomSheetDialog(Shoe shoe);
    }
    // Constructor mới với cờ viewType
    public ShoeAdapter(Context context, List<Shoe> popularShoesList, List<Shoe> allShoesList, String viewType, ExploreFragment exploreFragment,BottomSheetListener listener) {
        this.context = context;
        this.popularShoesList = popularShoesList;
        this.allShoesList = allShoesList;
        this.viewType = viewType;
        this.exploreFragment = exploreFragment;
        this.listener=listener;
    }
    //Constructor mới cho FavouriteFragment
    public ShoeAdapter(Context context, List<Shoe> popularShoesList, List<Shoe> allShoesList, String viewType, FavouriteFragment favouriteFragment,BottomSheetListener listener) {
        this.context = context;
        this.popularShoesList = popularShoesList;
        this.allShoesList = allShoesList;
        this.viewType = viewType;
        this.favouriteFragment = favouriteFragment;
        this.listener=listener;
    }
    public ShoeAdapter(Context context, List<Shoe> popularShoesList, List<Shoe> allShoesList, String viewType) {
        this.context = context;
        this.popularShoesList = popularShoesList;
        this.allShoesList = allShoesList;
        this.viewType = viewType;
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shoe, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Shoe shoe;
        // Dựa vào viewType để quyết định bind dữ liệu từ danh sách nào
        if (viewType.equals("popular")) {
            shoe = popularShoesList.get(position);
        } else {
            shoe = allShoesList.get(position);
        }

        // Hiển thị giá gốc và giá sau giảm
        double finalPrice = shoe.getPrice();
        Promotion promotion = shoe.getPromotion();

        if (promotion != null && isPromotionActive(promotion)) {
            finalPrice = shoe.getPrice() * (1 - promotion.getDiscount() / 100.0);
        }

        // Cập nhật dữ liệu cho các TextView và ImageView
        holder.titleTextView.setText(shoe.getTitle());
        holder.priceTextView.setText("$" + String.format("%.2f", finalPrice));

        // Sử dụng Picasso để tải ảnh từ URL
        if (shoe.getPicUrl() != null && !shoe.getPicUrl().isEmpty()) {
            Picasso.get().load(shoe.getPicUrl().get(0)).into(holder.itemImageView);
        }

        // Cập nhật trạng thái của biểu tượng yêu thích
        if (shoe.isFavourite()) {
            holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.heartIcon.setImageResource(R.drawable.ic_heart);
        }

        // Xử lý sự kiện click cho nút yêu thích
        holder.heartIcon.setOnClickListener(v -> {
            toggleFavourite(shoe, holder);
        });

        // Xử lý sự kiện click vào item để hiển thị chi tiết sản phẩm
        holder.itemView.setOnClickListener(v -> {
            // Tạo Intent để điều hướng đến ProductDetailActivity
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("productId", shoe.getProductId()); // Truyền productId vào Intent
            context.startActivity(intent);
        });
        //Xử lý sự kiện click nút thêm
        holder.btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.showBottomSheetDialog(shoe);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // Dựa vào viewType để trả về đúng số lượng phần tử
        if ("popular".equals(viewType) && popularShoesList != null) {
            return popularShoesList.size();
        } else if (allShoesList != null) {
            return allShoesList.size();
        } else {
            return 0;
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView, heartIcon;
        TextView titleTextView, priceTextView;
        ImageButton btnAddCart;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImage);
            titleTextView = itemView.findViewById(R.id.itemTitle);
            priceTextView = itemView.findViewById(R.id.itemPrice);
            heartIcon = itemView.findViewById(R.id.heartIcon);
            btnAddCart=itemView.findViewById(R.id.addToCartButton);
        }
    }

    // Cập nhật trạng thái yêu thích của một sản phẩm giày và đồng bộ giữa cả hai danh sách
    private void toggleFavourite(Shoe shoe, ItemViewHolder holder) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in to manage your favourites.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference userFavouriteRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("favourite");

        if (shoe.isFavourite()) {
            // Nếu sản phẩm đã được yêu thích, thực hiện bỏ yêu thích
            userFavouriteRef.child(shoe.getProductId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        shoe.setFavourite(false);
                        holder.heartIcon.setImageResource(R.drawable.ic_heart);
                        //Toast.makeText(context, "Removed from favourites.", Toast.LENGTH_SHORT).show();

                        // Đồng bộ trạng thái yêu thích giữa cả hai danh sách
                        syncFavouriteStatus(shoe);
                    })
                    .addOnFailureListener(e -> {
                        //Toast.makeText(context, "Failed to remove from favourites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Nếu sản phẩm chưa được yêu thích, thực hiện thêm vào yêu thích
            Map<String, Object> favouriteItem = new HashMap<>();
            favouriteItem.put("productId", shoe.getProductId());
            favouriteItem.put("title", shoe.getTitle());

            userFavouriteRef.child(shoe.getProductId()).setValue(favouriteItem)
                    .addOnSuccessListener(aVoid -> {
                        shoe.setFavourite(true);
                        holder.heartIcon.setImageResource(R.drawable.ic_heart_filled);
                        //Toast.makeText(context, "Added to favourites.", Toast.LENGTH_SHORT).show();

                        // Đồng bộ trạng thái yêu thích giữa cả hai danh sách
                        syncFavouriteStatus(shoe);
                    })
                    .addOnFailureListener(e -> {
                        //Toast.makeText(context, "Failed to add to favourites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void syncFavouriteStatus(Shoe shoe) {
        // Đồng bộ trạng thái yêu thích trong danh sách All Shoes
        if (allShoesList != null) {
            for (Shoe item : allShoesList) {
                if (item.getProductId().equals(shoe.getProductId())) {
                    item.setFavourite(shoe.isFavourite());
                    break;
                }
            }
        }

        // Đồng bộ trạng thái yêu thích trong danh sách Popular Shoes
        if (popularShoesList != null) {
            for (Shoe item : popularShoesList) {
                if (item.getProductId().equals(shoe.getProductId())) {
                    item.setFavourite(shoe.isFavourite());
                    break;
                }
            }
        }

        // Gọi phương thức updateAdapters() từ ExploreFragment để cập nhật cả hai RecyclerView
        if (exploreFragment != null) {
            exploreFragment.updateAdapters();
        }
    }

    // Kiểm tra ngày khuyến mãi
    private boolean isPromotionActive(Promotion promotion) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(promotion.getStartDate());
            Date endDate = sdf.parse(promotion.getEndDate());
            Date today = new Date();
            today = resetTime(today);
            return (today.after(startDate) || today.equals(startDate)) && (today.before(endDate) || today.equals(endDate));
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
