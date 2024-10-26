package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.footfitstore.R;
import com.example.footfitstore.adapter.SizeAdapter;
import com.example.footfitstore.fragment.CartFragment;
import com.example.footfitstore.fragment.ExploreFragment;
import com.example.footfitstore.fragment.FavouriteFragment;
import com.example.footfitstore.fragment.ProfileFragment;
import com.example.footfitstore.model.Shoe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mainFrame;
    private BottomNavigationView bottomNavigationView;
    private FirebaseUser currentUser;
    private String selectedSize;
    private String productId;
    private String productName;
    private double price = 0;
    private DatabaseReference userCartRef;
    private DatabaseReference productReference;
    private SizeAdapter sizeAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFrame = findViewById(R.id.main_frame);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userCartRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("cart");
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_explore) {
                    setFragment(new ExploreFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_favourite) {
                    setFragment(new FavouriteFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_cart) {
                    setFragment(new CartFragment());
                    return true;
//                } else if (item.getItemId() == R.id.nav_notifications) {
//                    setFragment(new NotificationsFragment());
//                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    setFragment(new ProfileFragment());
                    return true;
                } else {
                    return false;
                }
            }
        });


        // Kiểm tra intent để mở CartFragment nếu cần
        Intent intent = getIntent();
        if (intent != null && "cart".equals(intent.getStringExtra("openFragment"))) {
            bottomNavigationView.setSelectedItemId(R.id.nav_cart);  // Đặt mục nav_cart được chọn
            bottomNavigationView.getMenu().performIdentifierAction(R.id.nav_cart, 0);  // Kích hoạt sự kiện để chuyển sang CartFragment
        } else {
            // Set default fragment là ExploreFragment nếu không có yêu cầu đặc biệt
            setFragment(new ExploreFragment());
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    public void setSelectedNavItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);  // Thay đổi trạng thái của BottomNavigationView
    }
    //Khởi tạo và thêm list size vào bottom sheet dialog
    public void showBottomSheetDialog(Shoe shoe) {
        productId=shoe.getProductId();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_size_pick, null);
        recyclerView=dialogView.findViewById(R.id.size);
        String productId=shoe.getProductId();
        loadProductData(productId);
        Button btnSubmit=dialogView.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSize==null)
                {
                    Toast.makeText(MainActivity.this, "Please select a size", Toast.LENGTH_SHORT).show();
                }
                else {
                    addToCart();
                }
            }
        });
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }
    //Lấy thông tin Sản phẩm
    private void loadProductData(String productId) {
        productReference = FirebaseDatabase.getInstance().getReference("Shoes").child(productId);

        productReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    productName = dataSnapshot.child("title").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    price = dataSnapshot.child("price").getValue(Double.class);
                    // Tải kích cỡ giày từ Firebase
                    List<String> sizes = new ArrayList<>();
                    for (DataSnapshot sizeSnapshot : dataSnapshot.child("size").getChildren()) {
                        sizes.add(sizeSnapshot.getValue(String.class));
                    }
                    setupSizeRecyclerView(sizes);  // Hiển thị các kích cỡ vào RecyclerView
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load product data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Khởi tạo sizeAdapter
    private void setupSizeRecyclerView(List<String> sizes) {
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
        sizeAdapter = new SizeAdapter(MainActivity.this, sizes);
        recyclerView.setAdapter(sizeAdapter);
        sizeAdapter.setOnSizeSelectedListener(size -> selectedSize = size);
    }
    //Thêm vào giỏ
    private void addToCart() {
        // Tạo khóa mới cho sản phẩm trong giỏ hàng dựa trên productId và size
        String cartKey = productId + "_" + selectedSize;

        // Kiểm tra xem sản phẩm với kích cỡ đã có trong giỏ hàng chưa
        userCartRef.child(cartKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu sản phẩm đã có trong giỏ hàng, tăng số lượng
                    int currentQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                    userCartRef.child(cartKey).child("quantity").setValue(currentQuantity + 1)
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Increased quantity in cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to update cart", Toast.LENGTH_SHORT).show());
                } else {
                    // Nếu sản phẩm chưa có trong giỏ hàng, thêm sản phẩm mới với số lượng là 1
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("productId", productId);
                    cartItem.put("productName", productName);
                    cartItem.put("price", price);
                    cartItem.put("quantity", 1);
                    cartItem.put("size", selectedSize);

                    userCartRef.child(cartKey).setValue(cartItem)
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Added to cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to check cart status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}