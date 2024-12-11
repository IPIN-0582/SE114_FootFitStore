package com.example.footfitstore.fragment.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.User.MainActivity;
import com.example.footfitstore.activity.User.PaymentActivity;
import com.example.footfitstore.adapter.UserSideAdapter.CartAdapter;
import com.example.footfitstore.model.Cart;
import com.example.footfitstore.model.Promotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private ImageButton btnBack;
    private FirebaseUser currentUser;
    private DatabaseReference userCartRef;
    private List<Cart> cartList = new ArrayList<>();
    private double totalPrice = 0;
    private Boolean isInit=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_cart);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnCheckout = view.findViewById(R.id.checkout_button);
        btnBack = view.findViewById(R.id.btnBack);
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userCartRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("cart");

            // Load cart data from Firebase
            loadCartData();
        }
        cartAdapter = new CartAdapter(cartList, getContext(), new CartAdapter.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(int position, boolean isChecked) {
                Cart item = cartList.get(position);

                // Lấy dữ liệu khuyến mãi từ Firebase để tính toán
                DatabaseReference productRef = FirebaseDatabase.getInstance()
                        .getReference("Shoes")
                        .child(item.getProductId())
                        .child("promotion");

                productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double itemPrice = item.getPrice(); // Giá gốc
                        if (dataSnapshot.exists()) {
                            Promotion promotion = dataSnapshot.getValue(Promotion.class);
                            if (promotion != null && isPromotionActive(promotion)) {
                                double discount = promotion.getDiscount();
                                itemPrice = itemPrice * (1 - discount / 100); // Áp dụng giảm giá
                            }
                        }

                        // Cập nhật totalPrice khi sản phẩm được chọn hoặc bỏ chọn
                        if (isChecked) {
                            totalPrice += itemPrice * item.getQuantity();
                        } else {
                            totalPrice -= itemPrice * item.getQuantity();
                        }

                        // Hiển thị giá trị tổng
                        tvTotalPrice.setText("Total: $" + String.format("%.2f", totalPrice));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load promotion data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged(double price, int totalQuantity) {
                totalPrice = price;
                tvTotalPrice.setText("Total: $" + String.format("%.2f", totalPrice));
            }
        });
        recyclerView.setAdapter(cartAdapter);
        btnBack.setOnClickListener(v -> {
            // Chuyển đổi sang ExploreFragment
            ExploreFragment exploreFragment = new  ExploreFragment();

            // Sử dụng FragmentManager để thay thế fragment hiện tại bằng ExploreFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, exploreFragment)  // R.id.main_frame là ID của FrameLayout trong MainActivity
                    .addToBackStack(null)  // Nếu muốn cho phép quay lại
                    .commit();

            // Cập nhật BottomNavigationView
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_explore);  // Cập nhật trạng thái bottom nav
            }
        });

        // Handle checkout button click
        btnCheckout.setOnClickListener(v -> {
            List<Boolean> selectedList = cartAdapter.getSelectedList();
            boolean check = true;
            for (int i=0;i<selectedList.size();i++)
            {
                if (selectedList.get(i)) check=false;
            }
            if (check) {
                Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
             else {
               Intent intent = new Intent(getActivity(), PaymentActivity.class);
               intent.putExtra("total",totalPrice);
               startActivity(intent);
               String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
               DatabaseReference finalOrder = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
               finalOrder.child("FinalOrder").addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       for (int i=0;i<cartList.size();i++)
                       {
                           if (selectedList.get(i))
                           {
                               Cart cart = cartList.get(i);
                               String productKey = cart.getProductId() + "_" + cart.getSize();
                               Map<String, Object> cartItem = new HashMap<>();
                               cartItem.put("price",cart.getPrice());
                               cartItem.put("productId",cart.getProductId());
                               cartItem.put("productName",cart.getProductName());
                               cartItem.put("quantity",cart.getQuantity());
                               cartItem.put("size",cart.getSize());
                               finalOrder.child("FinalOrder").child(productKey).setValue(cartItem);
                           }
                       }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {

                   }
               });
            }
        });
        return view;
    }

    private void loadCartData() {
        userCartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartList.clear();

                for (DataSnapshot cartSnapshot : dataSnapshot.getChildren()) {
                    Cart cart = cartSnapshot.getValue(Cart.class);
                    cartList.add(cart);

                }
                if (!isInit)
                {
                    List<Boolean> myList = new ArrayList<>(Collections.nCopies(cartList.size(),false));
                    cartAdapter.setSelectedList(myList);
                    isInit=true;
                }
                // Update UI
                cartAdapter.notifyDataSetChanged();
                //cartAdapter.calculateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load cart data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kiểm tra ngày khuyến mãi
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
