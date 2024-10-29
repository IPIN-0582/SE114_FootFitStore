package com.example.footfitstore.fragment;

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
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.adapter.CartAdapter;
import com.example.footfitstore.model.Cart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
        cartAdapter = new CartAdapter(cartList, getContext(), new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged(double totalPrice, int totalQuantity) {
                // Cập nhật tổng giá vào TextView
                tvTotalPrice.setText("Total: $" + totalPrice);
            }
        });
        recyclerView.setAdapter(cartAdapter);

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
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                // Handle the checkout process
                Toast.makeText(getContext(), "Proceeding to checkout", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadCartData() {
        userCartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartList.clear();
                totalPrice = 0;

                for (DataSnapshot cartSnapshot : dataSnapshot.getChildren()) {
                    Cart cart = cartSnapshot.getValue(Cart.class);
                    cartList.add(cart);

                    // Calculate total price
                    totalPrice += cart.getPrice() * cart.getQuantity();
                }

                // Update UI
                cartAdapter.notifyDataSetChanged();
                cartAdapter.calculateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load cart data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
