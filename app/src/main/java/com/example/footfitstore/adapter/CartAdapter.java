package com.example.footfitstore.adapter;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.Cart;
import com.example.footfitstore.model.Promotion;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Boolean> selectedList;
    private List<Cart> cartItems;
    private Context context;
    private OnQuantityChangeListener onQuantityChangeListener;
    private OnCheckedChangeListener onCheckedChangeListener;
    private boolean checkActivity;
    public interface OnCheckedChangeListener {
        void onCheckedChanged(int position, boolean isChecked);
    }
    public interface OnQuantityChangeListener {
        void onQuantityChanged(double Price, int totalQuantity);
    }
    public CartAdapter(List<Cart> cartItems,Context context, boolean checkActivity)
    {
        this.selectedList=new ArrayList<>(Collections.nCopies(cartItems.size(),false));
        this.cartItems=cartItems;
        this.context=context;
        this.checkActivity=checkActivity;
    }
    public CartAdapter(List<Cart> cartItems, Context context, OnCheckedChangeListener onCheckedChangeListener, OnQuantityChangeListener onQuantityChangeListener) {
        this.selectedList=new ArrayList<>(Collections.nCopies(100,false));
        this.cartItems = cartItems;
        this.context = context;
        this.onCheckedChangeListener = onCheckedChangeListener;
        this.onQuantityChangeListener = onQuantityChangeListener;
        this.checkActivity=false;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart item = cartItems.get(position);

        // Kiểm tra và lấy dữ liệu khuyến mãi
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("Shoes")
                .child(item.getProductId())
                .child("promotion");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Promotion promotion = dataSnapshot.getValue(Promotion.class);
                    if (promotion != null && isPromotionActive(promotion)) {
                        // Áp dụng giá khuyến mãi
                        double discount = promotion.getDiscount();
                        double discountedPrice = item.getPrice() * (1 - discount / 100);

                        // Hiển thị giá gốc với gạch ngang và giá khuyến mãi
                        holder.tvProductOriginalPrice.setText("$" + String.format("%.2f", item.getPrice()));
                        holder.tvProductOriginalPrice.setPaintFlags(holder.tvProductOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        holder.tvProductOriginalPrice.setVisibility(View.VISIBLE);

                        holder.tvProductPrice.setText("$" + String.format("%.2f", discountedPrice));
                    } else {
                        // Nếu không có khuyến mãi, chỉ hiển thị giá gốc
                        holder.tvProductOriginalPrice.setVisibility(View.GONE);
                        holder.tvProductPrice.setText("$" + String.format("%.2f", item.getPrice()));
                    }
                } else {
                    // Nếu không có khuyến mãi, chỉ hiển thị giá gốc
                    holder.tvProductOriginalPrice.setVisibility(View.GONE);
                    holder.tvProductPrice.setText("$" + String.format("%.2f", item.getPrice()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load promotion data", Toast.LENGTH_SHORT).show();
            }
        });

        holder.tvProductName.setText(item.getProductName());
        holder.tvProductPrice.setText("$" + item.getPrice());
        holder.tvProductQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvProductSize.setText(item.getSize());
        holder.cbSelected.setOnCheckedChangeListener(null);
        holder.cbSelected.setChecked(selectedList.get(position));
        int positionNew = position;
        if (checkActivity)
        {
            holder.cbSelected.setVisibility(View.GONE);
            holder.btnDecrease.setVisibility(View.GONE);
            holder.btnIncrease.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
        holder.cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectedList.set(positionNew, isChecked);
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(positionNew, isChecked);
                }
            }
        });
        // Tải và hiển thị ảnh từ Firebase
        loadImageFromFirebase(item.getProductId(), holder.ivProductImage);

        // Xử lý tăng số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            updateProductQuantityInFirebase(item);
            calculateTotal();
        });

        // Xử lý giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                updateProductQuantityInFirebase(item);
                calculateTotal();
            }
        });

        // Xử lý xóa sản phẩm khỏi giỏ hàng
        holder.btnDelete.setOnClickListener(v -> {
            cartItems.remove(position);
            notifyItemRemoved(position);
            selectedList.remove(position);
            notifyItemChanged(position);
            notifyItemRangeChanged(position, cartItems.size());
            deleteProductFromFirebase(item);
            calculateTotal();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // Phương thức để tính toán tổng giá
    public void calculateTotal() {
        final double[] totalPrice = {0}; // Sử dụng mảng để có thể thay đổi giá trị
        final int[] totalQuantity = {0};

        List<Integer> selectedIndices = new ArrayList<>();
        for (int i = 0; i < selectedList.size(); i++) {
            if (selectedList.get(i)) selectedIndices.add(i);
        }
        if (selectedIndices.isEmpty())
        {
            if (onQuantityChangeListener != null) {
                onQuantityChangeListener.onQuantityChanged(totalPrice[0], totalQuantity[0]);
            }
        }
        for (Integer index : selectedIndices) {
            Cart item = cartItems.get(index);

            DatabaseReference productRef = FirebaseDatabase.getInstance()
                    .getReference("Shoes")
                    .child(item.getProductId())
                    .child("promotion");

            productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double discountedPrice = item.getPrice(); // Giá ban đầu
                    if (dataSnapshot.exists()) {
                        Promotion promotion = dataSnapshot.getValue(Promotion.class);
                        if (promotion != null && isPromotionActive(promotion)) {
                            double discount = promotion.getDiscount();
                            discountedPrice = item.getPrice() * (1 - discount / 100); // Giá sau giảm
                        }
                    }

                    totalPrice[0] += discountedPrice * item.getQuantity();
                    totalQuantity[0] += item.getQuantity();

                    // Gọi callback để cập nhật UI sau mỗi lần tính toán
                    if (onQuantityChangeListener != null) {
                        onQuantityChangeListener.onQuantityChanged(totalPrice[0], totalQuantity[0]);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to load promotion data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // Phương thức để lấy và hiển thị ảnh từ Firebase
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

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductSize, tvProductOriginalPrice;
        ImageView ivProductImage;
        ImageButton btnIncrease, btnDecrease, btnDelete;
        CheckBox cbSelected;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelected=itemView.findViewById(R.id.cb_selected);
            tvProductName = itemView.findViewById(R.id.item_name);
            tvProductPrice = itemView.findViewById(R.id.item_price);
            tvProductOriginalPrice= itemView.findViewById(R.id.item_original_price);
            tvProductQuantity = itemView.findViewById(R.id.item_quantity);
            tvProductSize = itemView.findViewById(R.id.item_size);
            ivProductImage = itemView.findViewById(R.id.item_image);  // Đảm bảo `ImageView` này có trong layout `item_cart.xml`
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private void updateProductQuantityInFirebase(Cart item) {
        String productKey = item.getProductId() + "_" + item.getSize();  // Kết hợp productId và size

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("cart")
                .child(productKey);  // Sử dụng productKey

        // Cập nhật số lượng trên Firebase
        cartRef.child("quantity").setValue(item.getQuantity()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Toast.makeText(context, "Quantity updated in Firebase", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to update quantity in Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProductFromFirebase(Cart item) {
        String productKey = item.getProductId() + "_" + item.getSize();  // Kết hợp productId và size

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("cart")
                .child(productKey);  // Sử dụng productKey

        // Xóa sản phẩm khỏi Firebase
        cartRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Toast.makeText(context, "Product removed from Firebase", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to remove product from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<Boolean> getSelectedList() {
        return selectedList;
    }

    // Kiểm tra ngày khuyến mãi
    private boolean isPromotionActive(Promotion promotion) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(promotion.getStartDate());
            Date endDate = sdf.parse(promotion.getEndDate());
            Date today = new Date();

            return today.after(startDate) && today.before(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}


