package com.example.footfitstore.adapter.UserSideAdapter;
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


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.Cart;
import com.example.footfitstore.model.Promotion;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Boolean> selectedList;
    private List<Cart> cartItems;
    private Context context;
    private OnQuantityChangeListener onQuantityChangeListener;
    private OnCheckedChangeListener onCheckedChangeListener;
    public interface OnCheckedChangeListener {
        void onCheckedChanged(int position, boolean isChecked);
    }
    public interface OnQuantityChangeListener {
        void onQuantityChanged(double Price, int totalQuantity);
    }

    public void setSelectedList(List<Boolean> selectedList) {
        this.selectedList = selectedList;
    }

    public CartAdapter(List<Cart> cartItems, Context context, OnCheckedChangeListener onCheckedChangeListener, OnQuantityChangeListener onQuantityChangeListener) {
        this.selectedList = new ArrayList<>();
        this.cartItems = cartItems;
        this.context = context;
        this.onCheckedChangeListener = onCheckedChangeListener;
        this.onQuantityChangeListener = onQuantityChangeListener;
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
                        double discount = promotion.getDiscount();
                        double discountedPrice = item.getPrice() * (1 - discount / 100);

                        holder.tvProductOriginalPrice.setText("$" + String.format("%.2f", item.getPrice()));
                        holder.tvProductOriginalPrice.setPaintFlags(holder.tvProductOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        holder.tvProductOriginalPrice.setVisibility(View.VISIBLE);

                        holder.tvProductPrice.setText("$" + String.format("%.2f", discountedPrice));
                    } else {
                        holder.tvProductOriginalPrice.setVisibility(View.GONE);
                        holder.tvProductPrice.setText("$" + String.format("%.2f", item.getPrice()));
                    }
                } else {
                    holder.tvProductOriginalPrice.setVisibility(View.GONE);
                    holder.tvProductPrice.setText("$" + String.format("%.2f", item.getPrice()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tvProductName.setText(item.getProductName());
        holder.tvProductPrice.setText("$" + item.getPrice());
        holder.tvProductQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvProductSize.setText(item.getSize());
        holder.cbSelected.setOnCheckedChangeListener(null);
        holder.cbSelected.setChecked(selectedList.get(position));
        int positionNew = position;
        holder.cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectedList.set(positionNew, isChecked);
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(positionNew, isChecked);
                }
            }
        });
        loadImageFromFirebase(item.getProductId(), holder.ivProductImage);

        holder.btnIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            updateProductQuantityInFirebase(item);
            calculateTotal();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                updateProductQuantityInFirebase(item);
                calculateTotal();
            }
        });

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

    public void calculateTotal() {
        final double[] totalPrice = {0};
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
                    double discountedPrice = item.getPrice();
                    if (dataSnapshot.exists()) {
                        Promotion promotion = dataSnapshot.getValue(Promotion.class);
                        if (promotion != null && isPromotionActive(promotion)) {
                            double discount = promotion.getDiscount();
                            discountedPrice = item.getPrice() * (1 - discount / 100);
                        }
                    }

                    totalPrice[0] += discountedPrice * item.getQuantity();
                    totalQuantity[0] += item.getQuantity();

                    if (onQuantityChangeListener != null) {
                        onQuantityChangeListener.onQuantityChanged(totalPrice[0], totalQuantity[0]);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
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
                    Picasso.get().load(imageUrl).into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
            ivProductImage = itemView.findViewById(R.id.item_image);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private void updateProductQuantityInFirebase(Cart item) {
        String productKey = item.getProductId() + "_" + item.getSize();

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("cart")
                .child(productKey);

        cartRef.child("quantity").setValue(item.getQuantity()).addOnCompleteListener(Task::isSuccessful);
    }

    private void deleteProductFromFirebase(Cart item) {
        String productKey = item.getProductId() + "_" + item.getSize();

        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("cart")
                .child(productKey);

        cartRef.removeValue().addOnCompleteListener(Task::isSuccessful);
    }

    public List<Boolean> getSelectedList() {
        return selectedList;
    }

    private boolean isPromotionActive(Promotion promotion) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(promotion.getStartDate());
            Date endDate = sdf.parse(promotion.getEndDate());
            Date today = new Date();
            today = resetTime(today);
            return (today.after(startDate) || today.equals(startDate)) && (today.before(endDate) || today.equals(endDate));
        } catch (ParseException e) {
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


