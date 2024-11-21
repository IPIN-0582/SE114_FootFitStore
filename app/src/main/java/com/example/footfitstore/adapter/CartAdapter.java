package com.example.footfitstore.adapter;
import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        double totalPrice = 0;
        int totalQuantity = 0;
        List<Integer> index=new ArrayList<>();
        for (int i=0;i<selectedList.size();i++)
        {
            if (selectedList.get(i)) index.add(i);
        }
        for (Integer x: index)
        {
            totalPrice += cartItems.get(x).getPrice() * cartItems.get(x).getQuantity();
            totalQuantity+=cartItems.get(x).getQuantity();
        }

        // Gọi lại hàm onQuantityChanged để cập nhật tổng giá và số lượng sản phẩm
        if (onQuantityChangeListener != null) {
            onQuantityChangeListener.onQuantityChanged(totalPrice, totalQuantity);
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
        TextView tvProductName, tvProductPrice, tvProductQuantity, tvProductSize;
        ImageView ivProductImage;
        ImageButton btnIncrease, btnDecrease, btnDelete;
        CheckBox cbSelected;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelected=itemView.findViewById(R.id.cb_selected);
            tvProductName = itemView.findViewById(R.id.item_name);
            tvProductPrice = itemView.findViewById(R.id.item_price);
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
                Toast.makeText(context, "Quantity updated in Firebase", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Product removed from Firebase", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to remove product from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<Boolean> getSelectedList() {
        return selectedList;
    }
}


