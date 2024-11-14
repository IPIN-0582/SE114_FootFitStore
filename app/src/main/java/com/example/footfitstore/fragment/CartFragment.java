package com.example.footfitstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.Api.CreateOrder;
import com.example.footfitstore.R;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.activity.PaymentActivity;
import com.example.footfitstore.adapter.CartAdapter;
import com.example.footfitstore.model.Cart;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

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
    //private List<Boolean> selectedList=new ArrayList<>(Collections.nCopies(1000,false));

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
                if (isChecked) {
                    totalPrice += cartList.get(position).getPrice() * cartList.get(position).getQuantity();
                } else
                    totalPrice -= cartList.get(position).getPrice() * cartList.get(position).getQuantity();
                tvTotalPrice.setText("Total: $" + totalPrice);
            }
        }, new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged(double Price, int totalQuantity) {
                totalPrice=Price;
                tvTotalPrice.setText("Total: $" + totalPrice);
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

}
